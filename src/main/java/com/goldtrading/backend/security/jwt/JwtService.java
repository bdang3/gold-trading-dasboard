package com.goldtrading.backend.security.jwt;

import com.goldtrading.backend.security.AppUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final SecretKey jwtSecretKey;

    @Value("${app.jwt.access-token-minutes:15}")
    private long accessTokenMinutes;

    @Value("${app.jwt.refresh-token-days:30}")
    private long refreshTokenDays;

    public String generateAccessToken(AppUserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId().toString())
                .claim("role", principal.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenMinutes, ChronoUnit.MINUTES)))
                .signWith(jwtSecretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("uid", userId.toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTokenDays, ChronoUnit.DAYS)))
                .signWith(jwtSecretKey)
                .compact();
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(jwtSecretKey).build().parseSignedClaims(token).getPayload();
        } catch (SignatureException ex) {
            throw new IllegalArgumentException("Invalid token signature");
        }
    }
}

