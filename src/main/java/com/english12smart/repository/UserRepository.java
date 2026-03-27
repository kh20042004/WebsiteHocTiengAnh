package com.english12smart.repository;

import com.english12smart.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ========== USER REPOSITORY ==========
 * Repository để tương tác với User collection trong MongoDB
 * Thay JpaRepository bằng MongoRepository
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // ========== FIND BY EMAIL ==========
    /**
     * Tìm user theo email
     * @param email - Email của user
     * @return User object nếu tìm thấy, null nếu không
     */
    User findByEmail(String email);

    /**
     * Tìm user by email + active status
     * @param email - Email
     * @return User nếu tìm thấy và isActive = true
     */
    User findByEmailAndIsActiveTrue(String email);

    // ========== FIND BY ROLE ==========
    /**
     * Tìm tất cả user theo role
     * @param role - STUDENT, TEACHER, ADMIN
     * @return List các user có role tương ứng
     */
    List<User> findByRole(String role);

    /**
     * Tìm tất cả teacher active
     * @return List teacher có isActive = true
     */
    List<User> findByRoleAndIsActiveTrue(String role);

    // ========== CUSTOM QUERIES ==========
    /**
     * Tìm user tạo trong N ngày gần nhất
     * @param timestamp - Timestamp (millis) từ N ngày trước
     * @return List user
     */
    @Query("{ 'createdAt': { $gte: ?0 } }")
    List<User> findRecentUsers(Long timestamp);

    /**
     * Tìm tất cả user ngoại trừ ADMIN
     * @return List non-admin users
     */
    @Query("{ 'role': { $ne: 'ADMIN' } }")
    List<User> findAllNonAdminUsers();

    /**
     * Tìm user có totalLearningMinutes > N
     * @param minutes - Số phút
     * @return List user đã học > N phút
     */
    @Query("{ 'totalLearningMinutes': { $gt: ?0 } }")
    List<User> findTopLearners(Integer minutes);

    /**
     * Tìm user có streak hiện tại > 0
     * @return List user đang có streak
     */
    @Query("{ 'currentStreak': { $gt: 0 } }")
    List<User> findActiveStreakUsers();

    /**
     * Tìm top N user theo XP (leaderboard)
     * @return List user top xp
     */
    @Query("{ }")
    List<User> findTopByOrderByTotalXPDesc();

    // ========== COUNT ==========
    /**
     * Đếm số user theo role
     * @param role - STUDENT, TEACHER, ADMIN
     * @return Số lượng user
     */
    long countByRole(String role);

    /**
     * Đếm số user active
     * @return Số lượng user isActive = true
     */
    long countByIsActiveTrue();

    /**
     * Đếm số giáo viên đang hoạt động
     * @param role - TEACHER
     * @return Số lượng giáo viên active
     */
    long countByRoleAndIsActiveTrue(String role);

    /**
     * Đếm số user mới trong N ngày
     * @param timestamp - Timestamp từ N ngày trước
     * @return Số lượng user
     */
    @Query(value = "{ 'createdAt': { $gte: ?0 } }", count = true)
    long countRecentUsers(Long timestamp);

    // ========== EXISTS ==========
    /**
     * Kiểm tra email có tồn tại không
     * @param email - Email cần check
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByEmail(String email);

    /** Tìm nhiều user theo danh sách ID */
    List<User> findByIdIn(List<String> ids);

    // ========== OPTIONAL FIND ==========
    /**
     * Tìm user by ID (Optional)
     * @param id - User ID
     * @return Optional<User>
     */
    Optional<User> findById(String id);

    // ========== DELETE ==========
    /**
     * Xóa user by ID
     * @param id - User ID
     */
    void deleteById(String id);

    /**
     * Xóa user by email
     * @param email - Email
     */
    void deleteByEmail(String email);

    // ========== UPDATE (Dùng Service để update) ==========
    /*
     * MongoDB update được handle bởi Service layer
     * Ví dụ:
     * 
     * User user = repository.findByEmail(email);
     * user.setFullName(newName);
     * user.setUpdatedAt(System.currentTimeMillis());
     * repository.save(user);
     */
}
