package com.goldtrading.backend.users.service;

import com.goldtrading.backend.common.exception.BusinessException;
import com.goldtrading.backend.users.domain.entity.User;
import com.goldtrading.backend.users.dto.request.UpdateProfileRequest;
import com.goldtrading.backend.users.dto.response.UserMeResponse;
import com.goldtrading.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User requireByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("ACCESS_DENIED", "User not found"));
    }

    public UserMeResponse me(String email) {
        User u = requireByEmail(email);
        return new UserMeResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getAddress(), u.getRole(),
                u.getStatus(), u.getPreferredLanguage(), u.getCreatedAt());
    }

    @Transactional
    public UserMeResponse updateMe(String email, UpdateProfileRequest req) {
        User u = requireByEmail(email);
        if (req.fullName() != null && !req.fullName().isBlank()) u.setFullName(req.fullName());
        if (req.phone() != null) u.setPhone(req.phone());
        if (req.address() != null) u.setAddress(req.address());
        if (req.preferredLanguage() != null) u.setPreferredLanguage(req.preferredLanguage());
        userRepository.save(u);
        return me(email);
    }
}

