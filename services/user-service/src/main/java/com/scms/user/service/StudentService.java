package com.scms.user.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.user.domain.entity.Student;
import com.scms.user.domain.entity.User;
import com.scms.user.repository.StudentRepository;
import com.scms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 학생 관리 서비스
 *
 * 주요 기능:
 * - 학생 정보 CRUD
 * - 학과별 학생 조회
 * - 학년별 학생 조회
 * - 재학생/졸업생 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    /**
     * 학생 정보 생성 (User 연결)
     */
    @Transactional
    public Student createStudent(Long userId, Student student) {
        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 정보 설정
        student.setUser(user);

        // 3. 저장
        Student savedStudent = studentRepository.save(student);
        log.info("학생 정보 생성 완료: userId={}, studentId={}", userId, savedStudent.getStudentId());

        return savedStudent;
    }

    /**
     * 학생 정보 조회 (User ID로)
     */
    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.STUDENT_NOT_FOUND,
                        "학생 정보를 찾을 수 없습니다: userId=" + userId));
    }

    /**
     * 학생 정보 조회 (Student ID로)
     */
    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ApiException(ErrorCode.STUDENT_NOT_FOUND,
                        "학생 정보를 찾을 수 없습니다: studentId=" + studentId));
    }

    /**
     * 학과별 학생 조회
     */
    public List<Student> getStudentsByDepartment(String department) {
        return studentRepository.findByDepartmentAndDeletedAtIsNull(department);
    }

    /**
     * 학년별 학생 조회
     */
    public List<Student> getStudentsByGrade(Integer grade) {
        return studentRepository.findByGradeAndDeletedAtIsNull(grade);
    }

    /**
     * 재학생 조회
     */
    public List<Student> getActiveStudents() {
        return studentRepository.findByIsGraduatedFalseAndDeletedAtIsNull();
    }

    /**
     * 졸업생 조회
     */
    public List<Student> getGraduatedStudents() {
        return studentRepository.findByIsGraduatedTrueAndDeletedAtIsNull();
    }

    /**
     * 학생 정보 수정
     */
    @Transactional
    public Student updateStudent(Long studentId, Student updateInfo) {
        Student student = getStudentById(studentId);

        // 수정 가능한 필드 업데이트
        if (updateInfo.getDepartment() != null) {
            student.setDepartment(updateInfo.getDepartment());
        }
        if (updateInfo.getMajor() != null) {
            student.setMajor(updateInfo.getMajor());
        }
        if (updateInfo.getGrade() != null) {
            student.setGrade(updateInfo.getGrade());
        }
        if (updateInfo.getAdmissionDate() != null) {
            student.setAdmissionDate(updateInfo.getAdmissionDate());
        }
        if (updateInfo.getGraduationDate() != null) {
            student.setGraduationDate(updateInfo.getGraduationDate());
        }
        if (updateInfo.getIsGraduated() != null) {
            student.setIsGraduated(updateInfo.getIsGraduated());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("학생 정보 수정 완료: studentId={}", studentId);

        return updatedStudent;
    }

    /**
     * 학생 졸업 처리
     */
    @Transactional
    public void graduateStudent(Long studentId) {
        Student student = getStudentById(studentId);
        student.setIsGraduated(true);
        studentRepository.save(student);
        log.info("학생 졸업 처리 완료: studentId={}", studentId);
    }

    /**
     * 학생 정보 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = getStudentById(studentId);
        student.delete();
        studentRepository.save(student);
        log.info("학생 정보 삭제 완료: studentId={}", studentId);
    }

    /**
     * 학과별 학생 수 조회
     */
    public long countByDepartment(String department) {
        return studentRepository.countByDepartmentAndDeletedAtIsNull(department);
    }

    /**
     * 학년별 학생 수 조회
     */
    public long countByGrade(Integer grade) {
        return studentRepository.countByGradeAndDeletedAtIsNull(grade);
    }

    /**
     * 전체 재학생 수
     */
    public long countActiveStudents() {
        return studentRepository.countByIsGraduatedFalseAndDeletedAtIsNull();
    }

    /**
     * 전체 졸업생 수
     */
    public long countGraduatedStudents() {
        return studentRepository.countByIsGraduatedTrueAndDeletedAtIsNull();
    }
}
