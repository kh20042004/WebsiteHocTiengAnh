package com.english12smart.repository;

import com.english12smart.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Lấy danh sách thông báo của 1 user, có phân trang
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Lấy danh sách toàn bộ thông báo của 1 user sắp xếp mới nhất
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Đếm số lượng thông báo chưa đọc
    long countByUserIdAndIsReadFalse(String userId);

    // Tìm tất cả thông báo chưa đọc của user
    List<Notification> findByUserIdAndIsReadFalse(String userId);
}
