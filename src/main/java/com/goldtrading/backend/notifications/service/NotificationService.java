package com.goldtrading.backend.notifications.service;

import com.goldtrading.backend.common.exception.BusinessException;
import com.goldtrading.backend.notifications.domain.entity.Notification;
import com.goldtrading.backend.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void create(UUID userId, String type, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        notificationRepository.save(n);
    }

    public List<Notification> list(UUID userId) { return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId); }

    @Transactional
    public void markRead(UUID id, UUID userId) {
        Notification n = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("ACCESS_DENIED", "Notification does not belong to user"));
        n.setReadAt(OffsetDateTime.now());
    }

    @Transactional
    public void markAllRead(UUID userId) {
        var list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setReadAt(OffsetDateTime.now()));
    }
}
