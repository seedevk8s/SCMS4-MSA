package com.scms.user.controller;

import com.scms.user.domain.enums.UserRole;
import com.scms.user.dto.request.UserCreateRequest;
import com.scms.user.dto.request.UserUpdateRequest;
import com.scms.user.dto.response.UserResponse;
import com.scms.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 사용자 관리 컨트롤러
 *
 * 엔드포인트:
 * - POST /api/users - 사용자 생성
 * - GET /api/users/{userId} - 사용자 조회
 * - GET /api/users - 전체 사용자 조회
 * - GET /api/users/role/{role} - 역할별 사용자 조회
 * - GET /api/users/student-num/{studentNum} - 학번으로 조회
 * - GET /api/users/email/{email} - 이메일로 조회
 * - PUT /api/users/{userId} - 사용자 수정
 * - DELETE /api/users/{userId} - 사용자 삭제
 * - POST /api/users/{userId}/restore - 사용자 복원
 * - POST /api/users/{userId}/lock - 계정 잠금
 * - POST /api/users/{userId}/unlock - 계정 잠금 해제
 * - GET /api/users/check/student-num - 학번 중복 체크
 * - GET /api/users/check/email - 이메일 중복 체크
 * - GET /api/users/search - 사용자 검색
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 생성 (관리자 권한 필요)
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse response = userService.createUser(request);
        log.info("사용자 생성 완료: userId={}, studentNum={}",
                response.getUserId(), response.getStudentNum());

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 조회 (ID)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 사용자 조회
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        log.info("전체 사용자 조회: count={}", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * 역할별 사용자 조회
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        log.info("역할별 사용자 조회: role={}, count={}", role, users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * 학번으로 사용자 조회
     */
    @GetMapping("/student-num/{studentNum}")
    public ResponseEntity<UserResponse> getUserByStudentNum(@PathVariable String studentNum) {
        UserResponse response = userService.getUserByStudentNum(studentNum);
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일로 사용자 조회
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse response = userService.updateUser(userId, request);
        log.info("사용자 정보 수정 완료: userId={}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 삭제 (Soft Delete)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        log.info("사용자 삭제 완료: userId={}", userId);
        return ResponseEntity.ok(Map.of("message", "사용자가 삭제되었습니다."));
    }

    /**
     * 사용자 복원
     */
    @PostMapping("/{userId}/restore")
    public ResponseEntity<UserResponse> restoreUser(@PathVariable Long userId) {
        UserResponse response = userService.restoreUser(userId);
        log.info("사용자 복원 완료: userId={}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 계정 잠금
     */
    @PostMapping("/{userId}/lock")
    public ResponseEntity<Map<String, String>> lockUser(@PathVariable Long userId) {
        userService.lockUser(userId);
        log.info("사용자 계정 잠금: userId={}", userId);
        return ResponseEntity.ok(Map.of("message", "계정이 잠겼습니다."));
    }

    /**
     * 계정 잠금 해제
     */
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<Map<String, String>> unlockUser(@PathVariable Long userId) {
        userService.unlockUser(userId);
        log.info("사용자 계정 잠금 해제: userId={}", userId);
        return ResponseEntity.ok(Map.of("message", "계정 잠금이 해제되었습니다."));
    }

    /**
     * 학번 중복 체크
     */
    @GetMapping("/check/student-num")
    public ResponseEntity<Map<String, Boolean>> checkStudentNum(@RequestParam String studentNum) {
        boolean available = userService.isStudentNumAvailable(studentNum);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * 이메일 중복 체크
     */
    @GetMapping("/check/email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * 사용자 검색 (이름 또는 학번)
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        List<UserResponse> users = userService.searchUsers(keyword);
        log.info("사용자 검색: keyword={}, count={}", keyword, users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * 잠긴 계정 목록 조회
     */
    @GetMapping("/locked")
    public ResponseEntity<List<UserResponse>> getLockedUsers() {
        List<UserResponse> users = userService.getLockedUsers();
        log.info("잠긴 계정 조회: count={}", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * 역할별 사용자 수 조회
     */
    @GetMapping("/count/role/{role}")
    public ResponseEntity<Map<String, Long>> countByRole(@PathVariable UserRole role) {
        long count = userService.countByRole(role);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
