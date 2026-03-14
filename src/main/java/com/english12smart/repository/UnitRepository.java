package com.english12smart.repository;

import com.english12smart.entity.Unit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ========== UNIT REPOSITORY ==========
 * Tương tác với collection 'units' trong MongoDB
 */
@Repository
public interface UnitRepository extends MongoRepository<Unit, String> {

    /**
     * Lấy tất cả Unit đang active, sắp xếp theo thứ tự
     */
    List<Unit> findByIsActiveTrueOrderByOrderIndexAsc();

    /**
     * Lấy tất cả Unit, sắp xếp theo thứ tự (dành cho admin/teacher)
     */
    List<Unit> findAllByOrderByOrderIndexAsc();

    /**
     * Tìm Unit theo cấp độ
     * @param level - Cấp độ (A1, A2, B1, B2)
     */
    List<Unit> findByLevelAndIsActiveTrueOrderByOrderIndexAsc(String level);

    /**
     * Kiểm tra orderIndex đã tồn tại chưa (tránh trùng số thứ tự)
     * @param orderIndex - Số thứ tự Unit
     */
    boolean existsByOrderIndex(Integer orderIndex);

    /**
     * Đếm số Unit đang active
     */
    long countByIsActiveTrue();

    /**
     * Tìm Unit theo người tạo
     * @param createdBy - ID của người tạo
     */
    List<Unit> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}
