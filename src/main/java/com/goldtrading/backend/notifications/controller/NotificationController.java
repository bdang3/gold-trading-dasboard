package com.goldtrading.backend.notifications.controller;

import com.goldtrading.backend.common.api.ApiResponse;
import com.goldtrading.backend.notifications.service.NotificationService;
import com.goldtrading.backend.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/my")
    public ApiResponse<?> my(Principal principal) { return ApiResponse.ok(notificationService.list(userService.requireByEmail(principal.getName()).getId())); }

    @PostMapping("/{id}/read")
    public ApiResponse<?> read(Principal principal, @PathVariable UUID id) {
        notificationService.markRead(id, userService.requireByEmail(principal.getName()).getId());
        return ApiResponse.ok("ok");
    }

    @PostMapping("/read-all")
    public ApiResponse<?> readAll(Principal principal) { notificationService.markAllRead(userService.requireByEmail(principal.getName()).getId()); return ApiResponse.ok("ok"); }
}
