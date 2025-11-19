package com.scms.user.repository;

import com.scms.user.domain.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학생 Repository
 * - 레거시 학생 정보 관리
 * - Soft Delete 지원
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * 학번으로 삭제되지 않은 학생 조회
     */
    Optional<Student> findByStudentIdAndDeletedAtIsNull(String studentId);

    /**
     * 이메일로 삭제되지 않은 학생 조회
     */
    Optional<Student> findByEmailAndDeletedAtIsNull(String email);

    /**
     * 학과별 학생 목록 조회 (삭제되지 않은 학생만)
     */
    List<Student> findByDepartmentAndDeletedAtIsNull(String department);

    /**
     * 학과별 학생 수 카운트 (삭제되지 않은 학생만)
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.department = :department AND s.deletedAt IS NULL")
    long countByDepartment(@Param("department") String department);

    /**
     * 삭제되지 않은 모든 학생 조회
     */
    @Query("SELECT s FROM Student s WHERE s.deletedAt IS NULL ORDER BY s.studentId")
    List<Student> findAllActive();

    /**
     * 학번 존재 여부 확인 (삭제된 학생 포함)
     */
    boolean existsByStudentId(String studentId);

    /**
     * 이메일 존재 여부 확인 (삭제된 학생 포함)
     */
    boolean existsByEmail(String email);

    /**
     * 학번 중복 체크 (삭제되지 않은 학생만)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.studentId = :studentId AND s.deletedAt IS NULL")
    boolean existsByStudentIdAndNotDeleted(@Param("studentId") String studentId);

    /**
     * 이메일 중복 체크 (삭제되지 않은 학생만)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.email = :email AND s.deletedAt IS NULL")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    /**
     * 재학 상태별 학생 목록 조회 (삭제되지 않은 학생만)
     */
    @Query("SELECT s FROM Student s WHERE s.status = :status AND s.deletedAt IS NULL ORDER BY s.department, s.grade")
    List<Student> findByStatus(@Param("status") String status);

    /**
     * 학년별 학생 목록 조회 (삭제되지 않은 학생만)
     */
    @Query("SELECT s FROM Student s WHERE s.grade = :grade AND s.deletedAt IS NULL ORDER BY s.department, s.name")
    List<Student> findByGrade(@Param("grade") String grade);

    /**
     * 학과와 학년으로 학생 조회
     */
    @Query("SELECT s FROM Student s WHERE s.department = :department AND s.grade = :grade AND s.deletedAt IS NULL ORDER BY s.name")
    List<Student> findByDepartmentAndGrade(@Param("department") String department, @Param("grade") String grade);

    /**
     * 이름으로 학생 검색 (부분 일치, 삭제되지 않은 학생만)
     */
    @Query("SELECT s FROM Student s WHERE s.name LIKE %:name% AND s.deletedAt IS NULL ORDER BY s.name")
    List<Student> searchByName(@Param("name") String name);

    /**
     * 학과와 재학 상태로 학생 조회
     */
    @Query("SELECT s FROM Student s WHERE s.department = :department AND s.status = :status AND s.deletedAt IS NULL ORDER BY s.grade, s.name")
    List<Student> findByDepartmentAndStatus(@Param("department") String department, @Param("status") String status);

    /**
     * 재학 상태별 학생 수 카운트
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.status = :status AND s.deletedAt IS NULL")
    long countByStatus(@Param("status") String status);

    /**
     * ID로 삭제되지 않은 학생 조회
     */
    @Query("SELECT s FROM Student s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Student> findByIdAndNotDeleted(@Param("id") Long id);
}
