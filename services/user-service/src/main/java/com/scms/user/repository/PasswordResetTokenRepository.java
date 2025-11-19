package com.scms.user.repository;

import com.scms.user.domain.entity.ExternalUser;
import com.scms.user.domain.entity.PasswordResetToken;
import com.scms.user.domain.entity.PasswordResetToken.TokenType;
import com.scms.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 비밀번호 재설정 토큰 Repository
 * - 이메일 기반 비밀번호 재설정 토큰 관리
 * - 내부 회원과 외부 회원 모두 지원
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    /**
     * 토큰으로 미사용 토큰 조회
     */
    Optional<PasswordResetToken> findByTokenAndUsedIsFalse(String token);

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :expiresAtBefore")
    void deleteByExpiresAtBefore(@Param("expiresAtBefore") LocalDateTime expiresAtBefore);

    /**
     * 토큰으로 조회 (사용 여부 무관)
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * 이메일로 미사용 토큰 목록 조회 (최신순)
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.email = :email AND prt.used = false ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findUnusedTokensByEmail(@Param("email") String email);

    /**
     * 사용자(내부 회원)의 미사용 토큰 목록 조회
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user AND prt.used = false ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findUnusedTokensByUser(@Param("user") User user);

    /**
     * 외부 회원의 미사용 토큰 목록 조회
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.externalUser = :externalUser AND prt.used = false ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findUnusedTokensByExternalUser(@Param("externalUser") ExternalUser externalUser);

    /**
     * 토큰과 이메일로 유효한 토큰 조회 (미사용 + 만료되지 않음)
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token AND prt.email = :email AND prt.used = false AND prt.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(
            @Param("token") String token,
            @Param("email") String email,
            @Param("now") LocalDateTime now
    );

    /**
     * 사용자(내부 회원)의 모든 토큰 목록 조회
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findByUser(@Param("user") User user);

    /**
     * 외부 회원의 모든 토큰 목록 조회
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.externalUser = :externalUser ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findByExternalUser(@Param("externalUser") ExternalUser externalUser);

    /**
     * 토큰 타입별 토큰 목록 조회
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.tokenType = :tokenType ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findByTokenType(@Param("tokenType") TokenType tokenType);

    /**
     * 만료된 토큰 목록 조회
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.expiresAt < :now ORDER BY prt.expiresAt DESC")
    List<PasswordResetToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 사용된 토큰 목록 조회
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.used = true ORDER BY prt.usedAt DESC")
    List<PasswordResetToken> findUsedTokens();

    /**
     * 특정 기간 동안 생성된 토큰 목록 조회
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.createdAt BETWEEN :startDate AND :endDate ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 사용자의 유효한 토큰 존재 여부 확인 (내부 회원)
     */
    @Query("SELECT CASE WHEN COUNT(prt) > 0 THEN true ELSE false END FROM PasswordResetToken prt WHERE prt.user = :user AND prt.used = false AND prt.expiresAt > :now")
    boolean existsValidTokenByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * 외부 회원의 유효한 토큰 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(prt) > 0 THEN true ELSE false END FROM PasswordResetToken prt WHERE prt.externalUser = :externalUser AND prt.used = false AND prt.expiresAt > :now")
    boolean existsValidTokenByExternalUser(@Param("externalUser") ExternalUser externalUser, @Param("now") LocalDateTime now);

    /**
     * 이메일의 유효한 토큰 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(prt) > 0 THEN true ELSE false END FROM PasswordResetToken prt WHERE prt.email = :email AND prt.used = false AND prt.expiresAt > :now")
    boolean existsValidTokenByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    /**
     * 사용자(내부 회원)의 모든 미사용 토큰 사용 처리
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true, prt.usedAt = :now WHERE prt.user = :user AND prt.used = false")
    void markAllUserTokensAsUsed(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * 외부 회원의 모든 미사용 토큰 사용 처리
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true, prt.usedAt = :now WHERE prt.externalUser = :externalUser AND prt.used = false")
    void markAllExternalUserTokensAsUsed(@Param("externalUser") ExternalUser externalUser, @Param("now") LocalDateTime now);

    /**
     * 사용자(내부 회원)의 모든 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.user = :user")
    void deleteByUser(@Param("user") User user);

    /**
     * 외부 회원의 모든 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.externalUser = :externalUser")
    void deleteByExternalUser(@Param("externalUser") ExternalUser externalUser);

    /**
     * 특정 시간 이전에 생성된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.createdAt < :createdAtBefore")
    void deleteByCreatedAtBefore(@Param("createdAtBefore") LocalDateTime createdAtBefore);

    /**
     * 사용된 토큰 모두 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.used = true")
    void deleteAllUsedTokens();
}
