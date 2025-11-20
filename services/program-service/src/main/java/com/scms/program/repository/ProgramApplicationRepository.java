package com.scms.program.repository;

import com.scms.program.domain.entity.ProgramApplication;
import com.scms.program.domain.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 프로그램 신청 Repository
 */
@Repository
public interface ProgramApplicationRepository extends JpaRepository<ProgramApplication, Long> {

    /**
     * 프로그램별 신청 목록
     */
    List<ProgramApplication> findByProgramIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long programId);

    /**
     * 사용자별 신청 목록
     */
    List<ProgramApplication> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    /**
     * 프로그램 + 사용자로 조회 (중복 신청 체크)
     */
    Optional<ProgramApplication> findByProgramIdAndUserIdAndDeletedAtIsNull(Long programId, Long userId);

    /**
     * 중복 신청 여부
     */
    boolean existsByProgramIdAndUserIdAndDeletedAtIsNull(Long programId, Long userId);

    /**
     * 상태별 신청 목록 (프로그램)
     */
    List<ProgramApplication> findByProgramIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long programId,
            ApplicationStatus status
    );

    /**
     * 상태별 신청 목록 (사용자)
     */
    List<ProgramApplication> findByUserIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long userId,
            ApplicationStatus status
    );

    /**
     * 승인된 신청 목록 (프로그램)
     */
    @Query("SELECT a FROM ProgramApplication a WHERE a.programId = :programId " +
            "AND a.status IN ('APPROVED', 'ATTENDED', 'COMPLETED') " +
            "AND a.deletedAt IS NULL ORDER BY a.createdAt ASC")
    List<ProgramApplication> findApprovedApplications(@Param("programId") Long programId);

    /**
     * 출석자 목록
     */
    List<ProgramApplication> findByProgramIdAndAttendedTrueAndDeletedAtIsNullOrderByAttendedAtDesc(Long programId);

    /**
     * 수료자 목록
     */
    List<ProgramApplication> findByProgramIdAndCompletedTrueAndDeletedAtIsNullOrderByCompletedAtDesc(Long programId);

    /**
     * 프로그램별 신청 수
     */
    long countByProgramIdAndDeletedAtIsNull(Long programId);

    /**
     * 프로그램별 승인된 신청 수
     */
    @Query("SELECT COUNT(a) FROM ProgramApplication a WHERE a.programId = :programId " +
            "AND a.status IN ('APPROVED', 'ATTENDED', 'COMPLETED') AND a.deletedAt IS NULL")
    long countApprovedApplications(@Param("programId") Long programId);

    /**
     * 사용자별 수료한 프로그램 수
     */
    long countByUserIdAndCompletedTrueAndDeletedAtIsNull(Long userId);
}
