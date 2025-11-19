package com.scms.user.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.user.domain.entity.Counselor;
import com.scms.user.domain.entity.User;
import com.scms.user.repository.CounselorRepository;
import com.scms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 상담사 관리 서비스
 *
 * 주요 기능:
 * - 상담사 정보 CRUD
 * - 전문 분야별 상담사 조회
 * - 가용 상담사 조회
 * - 상담 통계
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorService {

    private final CounselorRepository counselorRepository;
    private final UserRepository userRepository;

    /**
     * 상담사 정보 생성 (User 연결)
     */
    @Transactional
    public Counselor createCounselor(Long userId, Counselor counselor) {
        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 상담사 정보 설정
        counselor.setUser(user);

        // 3. 저장
        Counselor savedCounselor = counselorRepository.save(counselor);
        log.info("상담사 정보 생성 완료: userId={}, counselorId={}",
                userId, savedCounselor.getCounselorId());

        return savedCounselor;
    }

    /**
     * 상담사 정보 조회 (User ID로)
     */
    public Counselor getCounselorByUserId(Long userId) {
        return counselorRepository.findByUserUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.COUNSELOR_NOT_FOUND,
                        "상담사 정보를 찾을 수 없습니다: userId=" + userId));
    }

    /**
     * 상담사 정보 조회 (Counselor ID로)
     */
    public Counselor getCounselorById(Long counselorId) {
        return counselorRepository.findById(counselorId)
                .orElseThrow(() -> new ApiException(ErrorCode.COUNSELOR_NOT_FOUND,
                        "상담사 정보를 찾을 수 없습니다: counselorId=" + counselorId));
    }

    /**
     * 전체 상담사 조회
     */
    public List<Counselor> getAllCounselors() {
        return counselorRepository.findAllActive();
    }

    /**
     * 가용 상담사 조회 (현재 활동 중)
     */
    public List<Counselor> getAvailableCounselors() {
        return counselorRepository.findByIsAvailableTrueAndDeletedAtIsNull();
    }

    /**
     * 전문 분야별 상담사 조회
     */
    public List<Counselor> getCounselorsBySpecialty(String specialty) {
        return counselorRepository.findBySpecialtyContainingAndDeletedAtIsNull(specialty);
    }

    /**
     * 상담사 정보 수정
     */
    @Transactional
    public Counselor updateCounselor(Long counselorId, Counselor updateInfo) {
        Counselor counselor = getCounselorById(counselorId);

        // 수정 가능한 필드 업데이트
        if (updateInfo.getDepartment() != null) {
            counselor.setDepartment(updateInfo.getDepartment());
        }
        if (updateInfo.getOfficeLocation() != null) {
            counselor.setOfficeLocation(updateInfo.getOfficeLocation());
        }
        if (updateInfo.getSpecialty() != null) {
            counselor.setSpecialty(updateInfo.getSpecialty());
        }
        if (updateInfo.getIntroduction() != null) {
            counselor.setIntroduction(updateInfo.getIntroduction());
        }
        if (updateInfo.getIsAvailable() != null) {
            counselor.setIsAvailable(updateInfo.getIsAvailable());
        }

        Counselor updatedCounselor = counselorRepository.save(counselor);
        log.info("상담사 정보 수정 완료: counselorId={}", counselorId);

        return updatedCounselor;
    }

    /**
     * 상담사 가용 상태 변경
     */
    @Transactional
    public void updateAvailability(Long counselorId, boolean isAvailable) {
        Counselor counselor = getCounselorById(counselorId);
        counselor.setIsAvailable(isAvailable);
        counselorRepository.save(counselor);
        log.info("상담사 가용 상태 변경: counselorId={}, isAvailable={}", counselorId, isAvailable);
    }

    /**
     * 상담 횟수 증가
     */
    @Transactional
    public void incrementConsultationCount(Long counselorId) {
        Counselor counselor = getCounselorById(counselorId);
        counselor.setTotalConsultations(counselor.getTotalConsultations() + 1);
        counselorRepository.save(counselor);
        log.info("상담 횟수 증가: counselorId={}, totalConsultations={}",
                counselorId, counselor.getTotalConsultations());
    }

    /**
     * 상담사 정보 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteCounselor(Long counselorId) {
        Counselor counselor = getCounselorById(counselorId);
        counselor.delete();
        counselorRepository.save(counselor);
        log.info("상담사 정보 삭제 완료: counselorId={}", counselorId);
    }

    /**
     * 전문 분야별 상담사 수
     */
    public long countBySpecialty(String specialty) {
        return counselorRepository.countBySpecialtyContainingAndDeletedAtIsNull(specialty);
    }

    /**
     * 가용 상담사 수
     */
    public long countAvailableCounselors() {
        return counselorRepository.countByIsAvailableTrueAndDeletedAtIsNull();
    }

    /**
     * 상담사 통계 (평균 상담 횟수)
     */
    public Double getAverageConsultations() {
        List<Counselor> counselors = getAllCounselors();
        if (counselors.isEmpty()) {
            return 0.0;
        }

        return counselors.stream()
                .mapToInt(Counselor::getTotalConsultations)
                .average()
                .orElse(0.0);
    }

    /**
     * 최다 상담 상담사 조회
     */
    public Counselor getTopCounselor() {
        return counselorRepository.findTopByDeletedAtIsNullOrderByTotalConsultationsDesc()
                .orElse(null);
    }
}
