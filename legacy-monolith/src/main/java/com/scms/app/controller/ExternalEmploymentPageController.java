package com.scms.app.controller;

import com.scms.app.model.EmploymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 외부취업 활동 페이지 Controller
 */
@Controller
@RequestMapping("/external-employment")
@RequiredArgsConstructor
@Slf4j
public class ExternalEmploymentPageController {

    /**
     * 학생: 내 외부취업 활동 목록 페이지
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public String listPage(Model model) {
        log.info("외부취업 활동 목록 페이지 접근");

        // EmploymentType enum 전달
        model.addAttribute("employmentTypes", EmploymentType.values());

        return "external-employment/list";
    }

    /**
     * 학생: 외부취업 활동 등록 페이지
     */
    @GetMapping("/register")
    @PreAuthorize("hasRole('STUDENT')")
    public String registerPage(Model model) {
        log.info("외부취업 활동 등록 페이지 접근");

        model.addAttribute("employmentTypes", EmploymentType.values());

        return "external-employment/register";
    }

    /**
     * 학생: 외부취업 활동 상세 페이지
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public String detailPage(@PathVariable("id") Long employmentId, Model model) {
        log.info("외부취업 활동 상세 페이지 접근 - employmentId: {}", employmentId);

        model.addAttribute("employmentId", employmentId);
        model.addAttribute("employmentTypes", EmploymentType.values());

        return "external-employment/detail";
    }

    /**
     * 관리자: 외부취업 활동 관리 페이지
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPage(Model model) {
        log.info("외부취업 활동 관리 페이지 접근 (관리자)");

        model.addAttribute("employmentTypes", EmploymentType.values());

        return "external-employment/admin";
    }
}
