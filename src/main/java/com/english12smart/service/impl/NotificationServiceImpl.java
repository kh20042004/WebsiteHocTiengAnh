package com.english12smart.service.impl;

import com.english12smart.entity.Notification;
import com.english12smart.repository.NotificationRepository;
import com.english12smart.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Page<Notification> getUserNotifications(String userId, Pageable pageable) {
        log.info("Lấy danh sách thông báo cho user: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public Notification markAsRead(String notificationId, String userId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            // Đảm bảo user có quyền đánh dấu đã đọc (chỉ chính chủ mới được đánh dấu)
            if (notification.getUserId().equals(userId)) {
                notification.setIsRead(true);
                return notificationRepository.save(notification);
            }
        }
        return null;
    }

    @Override
    public void markAllAsRead(String userId) {
        log.info("Đánh dấu tất cả thông báo đã đọc cho user: {}", userId);
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        
        if (!unreadNotifications.isEmpty()) {
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    @Override
    public Notification createNotification(String userId, String title, String message, String type, String targetUrl) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .targetUrl(targetUrl)
                .isRead(false)
                .createdAt(System.currentTimeMillis())
                .build();
                
        return notificationRepository.save(notification);
    }
}
