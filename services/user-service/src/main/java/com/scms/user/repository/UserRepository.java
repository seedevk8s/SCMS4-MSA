package com.scms.user.repository;

import com.scms.user.domain.entity.User;
import com.scms.user.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자(내부 회원) Repository
 * - 학생, 상담사, 관리자 통합 관리
 * - Soft Delete 지원
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 학번으로 삭제되지 않은 사용자 조회
     */
    Optional<User> findByStudentNumAndDeletedAtIsNull(Integer studentNum);

    /**
     * 이메일로 삭제되지 않은 사용자 조회
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * 학번 존재 여부 확인 (삭제된 사용자 포함)
     */
    boolean existsByStudentNum(Integer studentNum);

    /**
     * 이메일 존재 여부 확인 (삭제된 사용자 포함)
     */
    boolean existsByEmail(String email);

    /**
     * 역할별 삭제되지 않은 사용자 목록 조회
     */
    List<User> findByRoleAndDeletedAtIsNull(UserRole role);

    /**
     * 역할별 사용자 수 카운트 (삭제되지 않은 사용자만)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    long countByRole(@Param("role") UserRole role);

    /**
     * 삭제되지 않은 모든 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findAllActive();

    /**
     * 잠긴 계정 목록 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.locked = true AND u.deletedAt IS NULL ORDER BY u.updatedAt DESC")
    List<User> findAllLockedUsers();

    /**
     * 학번 중복 체크 (삭제되지 않은 사용자만)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.studentNum = :studentNum AND u.deletedAt IS NULL")
    boolean existsByStudentNumAndNotDeleted(@Param("studentNum") Integer studentNum);

    /**
     * 이메일 중복 체크 (삭제되지 않은 사용자만)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    /**
     * 학과별 학생 목록 조회 (역할이 STUDENT이고 삭제되지 않은 사용자)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.department = :department AND u.deletedAt IS NULL ORDER BY u.grade, u.name")
    List<User> findStudentsByDepartment(@Param("department") String department);

    /**
     * 학년별 학생 목록 조회 (역할이 STUDENT이고 삭제되지 않은 사용자)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.grade = :grade AND u.deletedAt IS NULL ORDER BY u.department, u.name")
    List<User> findStudentsByGrade(@Param("grade") Integer grade);

    /**
     * 이름으로 사용자 검색 (부분 일치, 삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.deletedAt IS NULL ORDER BY u.name")
    List<User> searchByName(@Param("name") String name);

    /**
     * 사용자 ID로 삭제되지 않은 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdAndNotDeleted(@Param("userId") Integer userId);

    /**
     * 역할과 학과로 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.department = :department AND u.deletedAt IS NULL ORDER BY u.name")
    List<User> findByRoleAndDepartment(@Param("role") UserRole role, @Param("department") String department);

    /**
     * 로그인 실패 횟수가 임계값 이상인 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.failCnt >= :threshold AND u.deletedAt IS NULL ORDER BY u.failCnt DESC")
    List<User> findUsersWithFailCountAbove(@Param("threshold") Integer threshold);

    /**
     * 계정 잠금이 필요한 사용자 조회 (실패 횟수가 5 이상이지만 아직 잠기지 않은 사용자)
     */
    @Query("SELECT u FROM User u WHERE u.failCnt >= 5 AND u.locked = false AND u.deletedAt IS NULL")
    List<User> findUsersNeedingLock();
}
