package com.scms.user.repository;

import com.scms.user.domain.entity.Counselor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 상담사 Repository
 * - User 테이블과 1:1 관계
 * - Soft Delete 지원
 */
@Repository
public interface CounselorRepository extends JpaRepository<Counselor, Integer> {

    /**
     * 상담사 ID로 삭제되지 않은 상담사 조회
     */
    Optional<Counselor> findByCounselorIdAndDeletedAtIsNull(Integer counselorId);

    /**
     * 삭제되지 않은 모든 상담사 조회
     */
    @Query("SELECT c FROM Counselor c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Counselor> findAllActive();

    /**
     * 전문 분야로 상담사 조회 (삭제되지 않은 상담사만)
     */
    @Query("SELECT c FROM Counselor c WHERE c.specialty = :specialty AND c.deletedAt IS NULL ORDER BY c.user.name")
    List<Counselor> findBySpecialty(@Param("specialty") String specialty);

    /**
     * 전문 분야로 상담사 검색 (부분 일치, 삭제되지 않은 상담사만)
     */
    @Query("SELECT c FROM Counselor c WHERE c.specialty LIKE %:specialty% AND c.deletedAt IS NULL ORDER BY c.user.name")
    List<Counselor> searchBySpecialty(@Param("specialty") String specialty);

    /**
     * 상담사 이름으로 검색 (부분 일치, 삭제되지 않은 상담사만)
     */
    @Query("SELECT c FROM Counselor c WHERE c.user.name LIKE %:name% AND c.deletedAt IS NULL ORDER BY c.user.name")
    List<Counselor> searchByName(@Param("name") String name);

    /**
     * ID로 삭제되지 않은 상담사 조회
     */
    @Query("SELECT c FROM Counselor c WHERE c.counselorId = :counselorId AND c.deletedAt IS NULL")
    Optional<Counselor> findByIdAndNotDeleted(@Param("counselorId") Integer counselorId);

    /**
     * 사용자 ID로 상담사 조회 (User의 ID로 조회)
     */
    @Query("SELECT c FROM Counselor c WHERE c.user.userId = :userId AND c.deletedAt IS NULL")
    Optional<Counselor> findByUserId(@Param("userId") Integer userId);

    /**
     * 활성 상담사 수 카운트 (삭제되지 않은 상담사만)
     */
    @Query("SELECT COUNT(c) FROM Counselor c WHERE c.deletedAt IS NULL")
    long countActive();

    /**
     * 전문 분야별 상담사 수 카운트
     */
    @Query("SELECT COUNT(c) FROM Counselor c WHERE c.specialty = :specialty AND c.deletedAt IS NULL")
    long countBySpecialty(@Param("specialty") String specialty);

    /**
     * 소개가 등록된 상담사 조회 (삭제되지 않은 상담사만)
     */
    @Query("SELECT c FROM Counselor c WHERE c.introduction IS NOT NULL AND c.introduction != '' AND c.deletedAt IS NULL ORDER BY c.user.name")
    List<Counselor> findCounselorsWithIntroduction();

    /**
     * 소개가 미등록된 상담사 조회 (삭제되지 않은 상담사만)
     */
    @Query("SELECT c FROM Counselor c WHERE (c.introduction IS NULL OR c.introduction = '') AND c.deletedAt IS NULL ORDER BY c.user.name")
    List<Counselor> findCounselorsWithoutIntroduction();

    /**
     * 학과별 상담사 조회 (User의 department 필드 활용)
     */
    @Query("SELECT c FROM Counselor c WHERE c.user.department = :department AND c.deletedAt IS NULL ORDER BY c.user.name")
    List<Counselor> findByDepartment(@Param("department") String department);

    /**
     * 잠기지 않은 활성 상담사 조회 (User의 locked 필드 활용)
     */
    @Query("SELECT c FROM Counselor c WHERE c.user.locked = false AND c.deletedAt IS NULL ORDER BY c.user.name")
    List<Counselor> findActiveAndUnlockedCounselors();

    /**
     * 상담사 존재 여부 확인 (User ID로)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Counselor c WHERE c.user.userId = :userId AND c.deletedAt IS NULL")
    boolean existsByUserId(@Param("userId") Integer userId);
}
