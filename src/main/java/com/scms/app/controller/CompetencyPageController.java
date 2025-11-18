package com.scms.app.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 역량진단 페이지 Controller
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CompetencyPageController {

    /**
     * 역량진단 결과 페이지
     */
    @GetMapping("/competency-result")
    public String competencyResult(HttpSession session, Model model) {
        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("역량진단 결과 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        log.info("역량진단 결과 페이지 접근: userId={}", userId);
        return "competency-result";
    }

    /**
     * 역량진단 페이지 (별칭)
     */
    @GetMapping("/assessment")
    public String assessment(HttpSession session) {
        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("역량진단 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        log.info("역량진단 페이지 접근: userId={} (redirect to result)", userId);
        // 현재는 결과 페이지로 리다이렉트
        return "redirect:/competency-result";
    }

    /**
     * 역량진단 입력 페이지 (미래 구현)
     */
    @GetMapping("/competency-assessment")
    public String competencyAssessment(HttpSession session, Model model) {
        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("역량진단 입력 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        log.info("역량진단 입력 페이지 접근: userId={}", userId);

        // TODO: 역량진단 입력 페이지 구현 시 활성화
        // 현재는 결과 페이지로 리다이렉트
        return "redirect:/competency-result";
    }
}
