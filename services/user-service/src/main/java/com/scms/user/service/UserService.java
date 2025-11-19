package com.scms.user.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.user.domain.entity.User;
import com.scms.user.domain.enums.UserRole;
import com.scms.user.dto.request.UserCreateRequest;
import com.scms.user.dto.request.UserUpdateRequest;
import com.scms.user.dto.response.UserResponse;
import com.scms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 관리 서비스
 *
 * 주요 기능:
 * - 사용자 CRUD
 * - 학번/이메일 중복 체크
 * - 계정 활성화/비활성화
 * - 계정 잠금/잠금 해제
 * - 역할별 사용자 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 생성
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // 1. 중복 체크
        if (userRepository.existsByStudentNum(request.getStudentNum())) {
            throw new ApiException(ErrorCode.DUPLICATE_STUDENT_NUM,
                    "이미 사용 중인 학번입니다: " + request.getStudentNum());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL,
                    "이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User 엔티티 생성
        User user = User.builder()
                .studentNum(request.getStudentNum())
                .password(encodedPassword)
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : UserRole.STUDENT)
                .locked(false)
                .failCnt(0)
                .passwordUpdatedAt(LocalDateTime.now())
                .build();

        // 4. 저장
        User savedUser = userRepository.save(user);
        log.info("사용자 생성 완료: userId={}, studentNum={}", savedUser.getUserId(), savedUser.getStudentNum());

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 조회 (ID)
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: userId=" + userId));

        if (user.isDeleted()) {
            throw new ApiException(ErrorCode.USER_DELETED, "삭제된 사용자입니다.");
        }

        return UserResponse.from(user);
    }

    /**
     * 사용자 조회 (학번)
     */
    public UserResponse getUserByStudentNum(String studentNum) {
        User user = userRepository.findByStudentNumAndDeletedAtIsNull(studentNum)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: studentNum=" + studentNum));

        return UserResponse.from(user);
    }

    /**
     * 사용자 조회 (이메일)
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: email=" + email));

        return UserResponse.from(user);
    }

    /**
     * 모든 사용자 조회 (삭제되지 않은)
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllActive()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 역할별 사용자 조회
     */
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRoleAndDeletedAtIsNull(role)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 수정
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new ApiException(ErrorCode.USER_DELETED, "삭제된 사용자는 수정할 수 없습니다.");
        }

        // 2. 이메일 중복 체크 (변경하는 경우)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ApiException(ErrorCode.DUPLICATE_EMAIL,
                        "이미 사용 중인 이메일입니다: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // 3. 정보 업데이트
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        // 4. 저장
        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료: userId={}", userId);

        return UserResponse.from(updatedUser);
    }

    /**
     * 사용자 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new ApiException(ErrorCode.USER_DELETED, "이미 삭제된 사용자입니다.");
        }

        user.delete();
        userRepository.save(user);
        log.info("사용자 삭제 완료 (Soft Delete): userId={}", userId);
    }

    /**
     * 사용자 복원
     */
    @Transactional
    public UserResponse restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!user.isDeleted()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "삭제되지 않은 사용자입니다.");
        }

        user.setDeletedAt(null);
        User restoredUser = userRepository.save(user);
        log.info("사용자 복원 완료: userId={}", userId);

        return UserResponse.from(restoredUser);
    }

    /**
     * 계정 잠금
     */
    @Transactional
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        user.lock();
        userRepository.save(user);
        log.info("사용자 계정 잠금: userId={}", userId);
    }

    /**
     * 계정 잠금 해제
     */
    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        user.unlock();
        userRepository.save(user);
        log.info("사용자 계정 잠금 해제: userId={}", userId);
    }

    /**
     * 학번 중복 체크
     */
    public boolean isStudentNumAvailable(String studentNum) {
        return !userRepository.existsByStudentNum(studentNum);
    }

    /**
     * 이메일 중복 체크
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 역할별 사용자 수 조회
     */
    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    /**
     * 잠긴 계정 목록 조회
     */
    public List<UserResponse> getLockedUsers() {
        return userRepository.findAllLockedUsers()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 검색 (이름 또는 학번)
     */
    public List<UserResponse> searchUsers(String keyword) {
        return userRepository.searchByNameOrStudentNum(keyword)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}
