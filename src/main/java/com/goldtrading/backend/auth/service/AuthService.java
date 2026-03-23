package com.goldtrading.backend.auth.service;

import com.goldtrading.backend.auth.domain.entity.PasswordResetToken;
import com.goldtrading.backend.auth.domain.entity.RefreshToken;
import com.goldtrading.backend.auth.dto.request.*;
import com.goldtrading.backend.auth.dto.response.AuthTokensResponse;
import com.goldtrading.backend.auth.repository.PasswordResetTokenRepository;
import com.goldtrading.backend.auth.repository.RefreshTokenRepository;
import com.goldtrading.backend.auditlogs.service.AuditLogService;
import com.goldtrading.backend.common.RoleType;
import com.goldtrading.backend.common.UserStatus;
import com.goldtrading.backend.common.exception.BusinessException;
import com.goldtrading.backend.notifications.service.NotificationService;
import com.goldtrading.backend.security.AppUserPrincipal;
import com.goldtrading.backend.security.jwt.JwtService;
import com.goldtrading.backend.users.domain.entity.User;
import com.goldtrading.backend.users.dto.response.UserMeResponse;
import com.goldtrading.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Transactional
    public UserMeResponse register(RegisterRequest req) {
        if (userRepository.findByEmailIgnoreCase(req.email()).isPresent()) {
            throw new BusinessException("EMAIL_EXISTS", "Email already registered");
        }
        User u = new User();
        u.setFullName(req.fullName());
        u.setEmail(req.email().toLowerCase());
        u.setPhone(req.phone());
        u.setAddress(req.address());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(RoleType.USER);
        u.setStatus(UserStatus.ACTIVE);
        u.setPreferredLanguage("vi");
        u.setFailedLoginCount(0);
        userRepository.save(u);
        notificationService.create(u.getId(), "success", "Đăng ký thành công", "Tài khoản của bạn đã được tạo thành công.");
        auditLogService.log("USER", u.getId().toString(), u.getFullName(), "REGISTER", "USER", u.getId().toString(), "success", "User registered", null);
        return new UserMeResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getAddress(), u.getRole(), u.getStatus(), u.getPreferredLanguage(), u.getCreatedAt());
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.email()).orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid credentials"));
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            throw new BusinessException("ACCOUNT_LOCKED", "Account temporarily locked");
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
            user.setFailedLoginCount(0);
        } catch (Exception ex) {
            int failed = user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount();
            failed++;
            user.setFailedLoginCount(failed);
            if (failed >= 5) {
                user.setLockedUntil(OffsetDateTime.now().plusMinutes(15));
            }
            auditLogService.log("USER", user.getId().toString(), user.getFullName(), "LOGIN", "USER", user.getId().toString(), "failed", "Login failed", Map.of("failedCount", failed));
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid credentials");
        }
        AppUserPrincipal principal = new AppUserPrincipal(user);
        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(hash(refresh));
        rt.setExpiresAt(OffsetDateTime.now().plusDays(30));
        refreshTokenRepository.save(rt);
        auditLogService.log("USER", user.getId().toString(), user.getFullName(), "LOGIN", "USER", user.getId().toString(), "success", "Login success", null);
        return new AuthTokensResponse(access, refresh, "Bearer", 900);
    }

    @Transactional
    public AuthTokensResponse refresh(RefreshRequest req) {
        var claims = jwtService.parse(req.refreshToken());
        String hash = hash(req.refreshToken());
        RefreshToken saved = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Invalid refresh token"));
        if (saved.getRevokedAt() != null || saved.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token expired or revoked");
        }

        UUID uid = UUID.fromString((String) claims.get("uid"));
        User user = userRepository.findById(uid).orElseThrow(() -> new BusinessException("ACCESS_DENIED", "User not found"));
        String access = jwtService.generateAccessToken(new AppUserPrincipal(user));
        String refresh = jwtService.generateRefreshToken(user.getId(), user.getEmail());
        saved.setRevokedAt(OffsetDateTime.now());

        RefreshToken next = new RefreshToken();
        next.setUserId(user.getId());
        next.setTokenHash(hash(refresh));
        next.setExpiresAt(OffsetDateTime.now().plusDays(30));
        refreshTokenRepository.save(next);
        return new AuthTokensResponse(access, refresh, "Bearer", 900);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresent(rt -> rt.setRevokedAt(OffsetDateTime.now()));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new BusinessException("ACCESS_DENIED", "User not found"));
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Current password incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        auditLogService.log("USER", user.getId().toString(), user.getFullName(), "CHANGE_PASSWORD", "USER", user.getId().toString(), "success", "Password changed", null);
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest req) {
        var maybeUser = userRepository.findByEmailIgnoreCase(req.email());
        if (maybeUser.isEmpty()) {
            return "If account exists, reset instructions were generated";
        }
        User user = maybeUser.get();
        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(OffsetDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(token);
        auditLogService.log("USER", user.getId().toString(), user.getFullName(), "FORGOT_PASSWORD", "USER", user.getId().toString(), "success", "Reset token issued", null);
        return rawToken;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(req.token()))
                .orElseThrow(() -> new BusinessException("RESET_TOKEN_INVALID", "Reset token invalid"));

        if (token.getUsedAt() != null) {
            throw new BusinessException("RESET_TOKEN_INVALID", "Reset token already used");
        }
        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("RESET_TOKEN_EXPIRED", "Reset token expired");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException("ACCESS_DENIED", "User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        token.setUsedAt(OffsetDateTime.now());

        auditLogService.log("USER", user.getId().toString(), user.getFullName(), "RESET_PASSWORD", "USER", user.getId().toString(), "success", "Password reset completed", null);
    }

    public UserMeResponse me(String email) {
        User u = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new BusinessException("ACCESS_DENIED", "User not found"));
        return new UserMeResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getAddress(), u.getRole(), u.getStatus(), u.getPreferredLanguage(), u.getCreatedAt());
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
