package com.goldtrading.backend.users.controller;

import com.goldtrading.backend.common.api.ApiResponse;
import com.goldtrading.backend.users.dto.request.UpdateProfileRequest;
import com.goldtrading.backend.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<?> me(Principal principal) { return ApiResponse.ok(userService.me(principal.getName())); }

    @PatchMapping("/me")
    public ApiResponse<?> patchMe(Principal principal, @RequestBody @Valid UpdateProfileRequest req) { return ApiResponse.ok(userService.updateMe(principal.getName(), req)); }
}

