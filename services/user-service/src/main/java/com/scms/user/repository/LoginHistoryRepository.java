package com.scms.user.repository;

import com.scms.user.domain.entity.LoginHistory;
import com.scms.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 로그인 이력 Repository
 * - 사용자의 로그인 시도 기록 관리
 * - 보안 감사 및 통계 분석
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {

    /**
     * 사용자별 로그인 이력 조회 (최신순)
     */
    List<LoginHistory> findByUserOrderByLoginAtDesc(User user);

    /**
     * 사용자별 로그인 성공/실패 이력 조회 (최신순)
     */
    List<LoginHistory> findByUserAndIsSuccessOrderByLoginAtDesc(User user, Boolean isSuccess);

    /**
     * 특정 시간 이후 로그인 성공 횟수 카운트
     */
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.user = :user AND lh.isSuccess = :isSuccess AND lh.loginAt >= :loginAtAfter")
    long countByUserAndIsSuccessAndLoginAtAfter(
            @Param("user") User user,
            @Param("isSuccess") Boolean isSuccess,
            @Param("loginAtAfter") LocalDateTime loginAtAfter
    );

    /**
     * 사용자 ID로 로그인 이력 조회 (최신순)
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user.userId = :userId ORDER BY lh.loginAt DESC")
    List<LoginHistory> findByUserId(@Param("userId") Integer userId);

    /**
     * 사용자 ID와 성공 여부로 로그인 이력 조회 (최신순)
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user.userId = :userId AND lh.isSuccess = :isSuccess ORDER BY lh.loginAt DESC")
    List<LoginHistory> findByUserIdAndIsSuccess(@Param("userId") Integer userId, @Param("isSuccess") Boolean isSuccess);

    /**
     * 특정 기간 동안의 로그인 이력 조회
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.loginAt BETWEEN :startDate AND :endDate ORDER BY lh.loginAt DESC")
    List<LoginHistory> findByLoginAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 IP 주소의 로그인 이력 조회
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.ipAddress = :ipAddress ORDER BY lh.loginAt DESC")
    List<LoginHistory> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 최근 N개의 로그인 이력 조회 (사용자별)
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user ORDER BY lh.loginAt DESC LIMIT :limit")
    List<LoginHistory> findRecentLoginsByUser(@Param("user") User user, @Param("limit") int limit);

    /**
     * 최근 로그인 실패 이력 조회 (사용자별, 제한된 개수)
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user AND lh.isSuccess = false ORDER BY lh.loginAt DESC LIMIT :limit")
    List<LoginHistory> findRecentFailedLoginsByUser(@Param("user") User user, @Param("limit") int limit);

    /**
     * 특정 기간 동안 로그인 성공 횟수 카운트
     */
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.isSuccess = true AND lh.loginAt BETWEEN :startDate AND :endDate")
    long countSuccessfulLoginsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 기간 동안 로그인 실패 횟수 카운트
     */
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.isSuccess = false AND lh.loginAt BETWEEN :startDate AND :endDate")
    long countFailedLoginsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 사용자별 총 로그인 횟수 카운트
     */
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.user = :user")
    long countByUser(@Param("user") User user);

    /**
     * 사용자별 로그인 성공 횟수 카운트
     */
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.user = :user AND lh.isSuccess = true")
    long countSuccessfulLoginsByUser(@Param("user") User user);

    /**
     * 사용자별 로그인 실패 횟수 카운트
     */
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.user = :user AND lh.isSuccess = false")
    long countFailedLoginsByUser(@Param("user") User user);

    /**
     * 최근 N일 동안의 로그인 이력 조회 (사용자별)
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user AND lh.loginAt >= :afterDate ORDER BY lh.loginAt DESC")
    List<LoginHistory> findRecentLoginsByUserSince(@Param("user") User user, @Param("afterDate") LocalDateTime afterDate);

    /**
     * 특정 IP에서 특정 시간 이후 실패한 로그인 시도 횟수 카운트
     */
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.ipAddress = :ipAddress AND lh.isSuccess = false AND lh.loginAt >= :afterDate")
    long countFailedLoginsByIpSince(@Param("ipAddress") String ipAddress, @Param("afterDate") LocalDateTime afterDate);

    /**
     * 사용자의 마지막 로그인 성공 이력 조회
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user AND lh.isSuccess = true ORDER BY lh.loginAt DESC LIMIT 1")
    LoginHistory findLastSuccessfulLoginByUser(@Param("user") User user);

    /**
     * 모든 로그인 이력 조회 (최신순)
     */
    @Query("SELECT lh FROM LoginHistory lh ORDER BY lh.loginAt DESC")
    List<LoginHistory> findAllOrderByLoginAtDesc();

    /**
     * 특정 기간 동안 특정 사용자의 로그인 이력 조회
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user AND lh.loginAt BETWEEN :startDate AND :endDate ORDER BY lh.loginAt DESC")
    List<LoginHistory> findByUserAndLoginAtBetween(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
