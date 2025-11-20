package com.scms.program.repository;

import com.scms.program.domain.entity.Program;
import com.scms.program.domain.enums.ProgramStatus;
import com.scms.program.domain.enums.ProgramType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 프로그램 Repository
 */
@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    /**
     * 프로그램 조회 (삭제되지 않은)
     */
    Optional<Program> findByProgramIdAndDeletedAtIsNull(Long programId);

    /**
     * 상태별 프로그램 목록
     */
    List<Program> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(ProgramStatus status);

    /**
     * 유형별 프로그램 목록
     */
    List<Program> findByTypeAndDeletedAtIsNullOrderByCreatedAtDesc(ProgramType type);

    /**
     * 카테고리별 프로그램 목록
     */
    List<Program> findByCategoryIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long categoryId);

    /**
     * 승인된 프로그램 목록 (모집중)
     */
    @Query("SELECT p FROM Program p WHERE p.status = 'APPROVED' AND p.deletedAt IS NULL " +
            "ORDER BY p.startDate ASC")
    List<Program> findApprovedPrograms();

    /**
     * 신청 가능한 프로그램 목록
     */
    @Query("SELECT p FROM Program p WHERE p.status = 'APPROVED' " +
            "AND p.currentParticipants < p.maxParticipants " +
            "AND p.deletedAt IS NULL " +
            "AND (p.applicationEndDate IS NULL OR p.applicationEndDate > :now) " +
            "ORDER BY p.applicationEndDate ASC")
    List<Program> findAvailablePrograms(@Param("now") LocalDateTime now);

    /**
     * 특정 기간 내 프로그램
     */
    @Query("SELECT p FROM Program p WHERE p.startDate >= :startDate " +
            "AND p.startDate <= :endDate AND p.deletedAt IS NULL " +
            "ORDER BY p.startDate ASC")
    List<Program> findProgramsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 제목 검색
     */
    @Query("SELECT p FROM Program p WHERE p.title LIKE %:keyword% " +
            "AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> searchByTitle(@Param("keyword") String keyword);

    /**
     * 담당자별 프로그램 목록
     */
    List<Program> findByCreatedByAndDeletedAtIsNullOrderByCreatedAtDesc(Long createdBy);

    /**
     * 마감 임박 프로그램 (D-3 이내)
     */
    @Query("SELECT p FROM Program p WHERE p.status = 'APPROVED' " +
            "AND p.applicationEndDate BETWEEN :now AND :threeDaysLater " +
            "AND p.deletedAt IS NULL ORDER BY p.applicationEndDate ASC")
    List<Program> findDeadlineSoonPrograms(
            @Param("now") LocalDateTime now,
            @Param("threeDaysLater") LocalDateTime threeDaysLater
    );

    /**
     * 인기 프로그램 (조회수 높은 순)
     */
    @Query("SELECT p FROM Program p WHERE p.status = 'APPROVED' " +
            "AND p.deletedAt IS NULL ORDER BY p.viewCount DESC")
    List<Program> findPopularPrograms();

    /**
     * 조회수 증가
     */
    @Modifying
    @Query("UPDATE Program p SET p.viewCount = p.viewCount + 1 WHERE p.programId = :programId")
    void incrementViewCount(@Param("programId") Long programId);

    /**
     * 신청 인원 증가
     */
    @Modifying
    @Query("UPDATE Program p SET p.currentParticipants = p.currentParticipants + 1 WHERE p.programId = :programId")
    void incrementParticipants(@Param("programId") Long programId);

    /**
     * 신청 인원 감소
     */
    @Modifying
    @Query("UPDATE Program p SET p.currentParticipants = p.currentParticipants - 1 " +
            "WHERE p.programId = :programId AND p.currentParticipants > 0")
    void decrementParticipants(@Param("programId") Long programId);

    /**
     * 전체 프로그램 수
     */
    long countByDeletedAtIsNull();

    /**
     * 상태별 프로그램 수
     */
    long countByStatusAndDeletedAtIsNull(ProgramStatus status);
}
