package com.scms.app.controller;

import com.scms.app.dto.ConsultationResponse;
import com.scms.app.model.ConsultationStatus;
import com.scms.app.model.UserRole;
import com.scms.app.service.ConsultationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 상담 페이지 Controller
 */
@Controller
@RequestMapping("/counseling")
@RequiredArgsConstructor
public class CounselingPageController {

    private final ConsultationService consultationService;

    /**
     * 상담 신청 페이지
     */
    @GetMapping
    public String consultationPage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        UserRole role = (UserRole) session.getAttribute("role");
        model.addAttribute("userRole", role);

        // 관리자 권한 설정
        if (session.getAttribute("isAdmin") == null && role != null) {
            session.setAttribute("isAdmin", role == UserRole.ADMIN);
        }

        model.addAttribute("pageTitle", "통합상담 신청");
        return "consultation";
    }

    /**
     * 상담 내역 페이지
     */
    @GetMapping("/list")
    public String consultationList(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        UserRole role = (UserRole) session.getAttribute("role");
        model.addAttribute("userRole", role);

        if (session.getAttribute("isAdmin") == null && role != null) {
            session.setAttribute("isAdmin", role == UserRole.ADMIN);
        }

        // 상담 내역 조회
        List<ConsultationResponse> consultations;
        if (status != null && !status.isEmpty()) {
            try {
                ConsultationStatus consultationStatus = ConsultationStatus.valueOf(status);
                consultations = consultationService.getMyConsultationsByStatus(userId, consultationStatus);
                model.addAttribute("currentFilter", status);
            } catch (IllegalArgumentException e) {
                consultations = consultationService.getMyConsultations(userId);
                model.addAttribute("currentFilter", "ALL");
            }
        } else {
            consultations = consultationService.getMyConsultations(userId);
            model.addAttribute("currentFilter", "ALL");
        }

        // 상태별 카운트 계산
        List<ConsultationResponse> allConsultations = consultationService.getMyConsultations(userId);
        long totalCount = allConsultations.size();
        long pendingCount = allConsultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.PENDING)
                .count();
        long approvedCount = allConsultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.APPROVED)
                .count();
        long completedCount = allConsultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.COMPLETED)
                .count();
        long rejectedCount = allConsultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.REJECTED)
                .count();
        long cancelledCount = allConsultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.CANCELLED)
                .count();

        model.addAttribute("consultations", consultations);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("pageTitle", "내 상담 내역");

        return "consultation-list";
    }

    /**
     * 상담 상세 페이지
     */
    @GetMapping("/{sessionId}")
    public String consultationDetail(
            @PathVariable Long sessionId,
            HttpSession session,
            Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        UserRole role = (UserRole) session.getAttribute("role");
        model.addAttribute("userRole", role);

        if (session.getAttribute("isAdmin") == null && role != null) {
            session.setAttribute("isAdmin", role == UserRole.ADMIN);
        }

        model.addAttribute("sessionId", sessionId);
        model.addAttribute("pageTitle", "상담 상세");
        return "consultation-detail";
    }

    /**
     * 상담사 관리 페이지 (상담사용)
     */
    @GetMapping("/manage")
    public String counselorManage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        UserRole role = (UserRole) session.getAttribute("role");
        if (role != UserRole.COUNSELOR && role != UserRole.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        model.addAttribute("userRole", role);

        if (session.getAttribute("isAdmin") == null && role != null) {
            session.setAttribute("isAdmin", role == UserRole.ADMIN);
        }

        model.addAttribute("pageTitle", "상담 관리");
        return "counselor/consultation-manage";
    }

    /**
     * 관리자 상담 관리 페이지
     */
    @GetMapping("/admin")
    public String adminConsultation(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        UserRole role = (UserRole) session.getAttribute("role");
        if (role != UserRole.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        model.addAttribute("userRole", role);
        session.setAttribute("isAdmin", true);

        model.addAttribute("pageTitle", "상담 관리 (관리자)");
        return "admin/consultation-admin";
    }

    /**
     * 관리자 상담사 관리 페이지
     */
    @GetMapping("/admin/counselors")
    public String adminCounselors(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        UserRole role = (UserRole) session.getAttribute("role");
        if (role != UserRole.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        model.addAttribute("userRole", role);
        session.setAttribute("isAdmin", true);

        model.addAttribute("pageTitle", "상담사 관리");
        return "admin/counselor-management";
    }
}
