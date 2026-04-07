package com.english12smart.service;

import com.english12smart.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    // Lấy thông báo theo User (có phân trang)
    Page<Notification> getUserNotifications(String userId, Pageable pageable);

    // Đếm số thông báo chưa đọc
    long getUnreadCount(String userId);

    // Đánh dấu 1 thông báo là đã đọc
    Notification markAsRead(String notificationId, String userId);

    // Đánh dấu tất cả thông báo là đã đọc
    void markAllAsRead(String userId);

    // Tạo thông báo mới
    Notification createNotification(String userId, String title, String message, String type, String targetUrl);
}
