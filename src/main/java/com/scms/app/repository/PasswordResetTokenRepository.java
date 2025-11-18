package com.scms.app.repository;

import com.scms.app.model.ExternalUser;
import com.scms.app.model.PasswordResetToken;
import com.scms.app.model.User;
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
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    /**
     * 토큰으로 조회
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * 유효한 토큰 조회 (사용되지 않고 만료되지 않은)
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.used = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * 내부 회원의 모든 토큰 조회
     */
    List<PasswordResetToken> findByUser(User user);

    /**
     * 외부 회원의 모든 토큰 조회
     */
    List<PasswordResetToken> findByExternalUser(ExternalUser externalUser);

    /**
     * 이메일로 유효한 토큰 조회
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.used = false AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    List<PasswordResetToken> findValidTokensByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 사용된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.used = true AND t.usedAt < :cutoffDate")
    void deleteUsedTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 특정 사용자의 모든 미사용 토큰 무효화
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true, t.usedAt = :now WHERE t.user = :user AND t.used = false")
    void invalidateAllUserTokens(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * 특정 외부 회원의 모든 미사용 토큰 무효화
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true, t.usedAt = :now WHERE t.externalUser = :externalUser AND t.used = false")
    void invalidateAllExternalUserTokens(@Param("externalUser") ExternalUser externalUser, @Param("now") LocalDateTime now);

    /**
     * 토큰 존재 여부 확인
     */
    boolean existsByToken(String token);
}
