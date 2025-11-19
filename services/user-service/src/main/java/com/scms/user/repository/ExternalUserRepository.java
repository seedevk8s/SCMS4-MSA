package com.scms.user.repository;

import com.scms.user.domain.entity.ExternalUser;
import com.scms.user.domain.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 외부 회원 Repository
 * - 이메일 기반 로그인 사용자
 * - 소셜 로그인(OAuth2) 지원
 * - Soft Delete 지원
 */
@Repository
public interface ExternalUserRepository extends JpaRepository<ExternalUser, Integer> {

    /**
     * 이메일로 삭제되지 않은 외부 회원 조회
     */
    Optional<ExternalUser> findByEmailAndDeletedAtIsNull(String email);

    /**
     * 소셜 로그인 제공자와 제공자 ID로 조회
     */
    Optional<ExternalUser> findByProviderAndProviderId(String provider, String providerId);

    /**
     * 이메일 존재 여부 확인 (삭제된 회원 포함)
     */
    boolean existsByEmail(String email);

    /**
     * 이메일 인증 토큰으로 조회
     */
    Optional<ExternalUser> findByEmailVerifyToken(String emailVerifyToken);

    /**
     * 계정 상태별 회원 수 카운트 (삭제되지 않은 회원만)
     */
    @Query("SELECT COUNT(e) FROM ExternalUser e WHERE e.status = :status AND e.deletedAt IS NULL")
    long countByStatus(@Param("status") AccountStatus status);

    /**
     * 삭제되지 않은 모든 외부 회원 조회
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalUser> findAllActive();

    /**
     * 이메일 중복 체크 (삭제되지 않은 회원만)
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM ExternalUser e WHERE e.email = :email AND e.deletedAt IS NULL")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    /**
     * 로그인 제공자별 회원 목록 조회 (삭제되지 않은 회원만)
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.provider = :provider AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalUser> findByProvider(@Param("provider") String provider);

    /**
     * 계정 상태별 회원 목록 조회 (삭제되지 않은 회원만)
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.status = :status AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalUser> findByStatus(@Param("status") AccountStatus status);

    /**
     * 잠긴 계정 목록 조회 (삭제되지 않은 회원만)
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.locked = true AND e.deletedAt IS NULL ORDER BY e.updatedAt DESC")
    List<ExternalUser> findAllLockedUsers();

    /**
     * 이메일 미인증 회원 목록 조회 (삭제되지 않은 회원만)
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.emailVerified = false AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalUser> findUnverifiedUsers();

    /**
     * 이름으로 회원 검색 (부분 일치, 삭제되지 않은 회원만)
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.name LIKE %:name% AND e.deletedAt IS NULL ORDER BY e.name")
    List<ExternalUser> searchByName(@Param("name") String name);

    /**
     * ID로 삭제되지 않은 회원 조회
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.userId = :userId AND e.deletedAt IS NULL")
    Optional<ExternalUser> findByIdAndNotDeleted(@Param("userId") Integer userId);

    /**
     * 로그인 제공자별 회원 수 카운트
     */
    @Query("SELECT COUNT(e) FROM ExternalUser e WHERE e.provider = :provider AND e.deletedAt IS NULL")
    long countByProvider(@Param("provider") String provider);

    /**
     * 소셜 로그인 사용자 목록 조회 (LOCAL이 아닌 모든 제공자)
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.provider != 'LOCAL' AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalUser> findAllSocialUsers();

    /**
     * 로컬 가입 사용자 목록 조회
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.provider = 'LOCAL' AND e.deletedAt IS NULL ORDER BY e.createdAt DESC")
    List<ExternalUser> findAllLocalUsers();

    /**
     * 로그인 실패 횟수가 임계값 이상인 회원 조회
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.failCnt >= :threshold AND e.deletedAt IS NULL ORDER BY e.failCnt DESC")
    List<ExternalUser> findUsersWithFailCountAbove(@Param("threshold") Integer threshold);

    /**
     * 계정 잠금이 필요한 회원 조회 (실패 횟수가 5 이상이지만 아직 잠기지 않은 회원)
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.failCnt >= 5 AND e.locked = false AND e.deletedAt IS NULL")
    List<ExternalUser> findUsersNeedingLock();

    /**
     * 소셜 로그인 제공자와 제공자 ID로 삭제되지 않은 회원 조회
     */
    @Query("SELECT e FROM ExternalUser e WHERE e.provider = :provider AND e.providerId = :providerId AND e.deletedAt IS NULL")
    Optional<ExternalUser> findByProviderAndProviderIdAndNotDeleted(@Param("provider") String provider, @Param("providerId") String providerId);
}
