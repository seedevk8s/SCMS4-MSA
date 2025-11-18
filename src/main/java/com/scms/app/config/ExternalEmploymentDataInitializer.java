package com.scms.app.config;

import com.scms.app.model.EmploymentType;
import com.scms.app.model.ExternalEmployment;
import com.scms.app.repository.ExternalEmploymentRepository;
import com.scms.app.service.ExternalEmploymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 외부취업 활동 테스트 데이터 초기화
 */
@Component
@Order(3) // MileageDataInitializer, SurveyDataInitializer 이후 실행
@RequiredArgsConstructor
@Slf4j
public class ExternalEmploymentDataInitializer implements CommandLineRunner {

    private final ExternalEmploymentRepository employmentRepository;
    private final ExternalEmploymentService employmentService;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (employmentRepository.count() > 0) {
            log.info("외부취업 활동 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
            return;
        }

        log.info("=== 외부취업 활동 테스트 데이터 삽입 시작 ===");

        // 이영희 학생 (userId=2)의 테스트 데이터 생성
        createSampleData();

        log.info("=== 외부취업 활동 테스트 데이터 삽입 완료: {} 개 활동 생성됨 ===", employmentRepository.count());
    }

    private void createSampleData() {
        // 1. 승인된 인턴십 (6개월, 150점)
        ExternalEmployment internship = ExternalEmployment.builder()
                .userId(2) // 이영희
                .employmentType(EmploymentType.INTERNSHIP)
                .companyName("(주)테크이노베이션")
                .position("백엔드 개발 인턴")
                .department("개발팀")
                .startDate(LocalDate.now().minusMonths(8))
                .endDate(LocalDate.now().minusMonths(2))
                .description("Java Spring Boot 기반 REST API 개발 및 데이터베이스 설계 업무를 수행했습니다.")
                .workContent("- 사용자 관리 REST API 개발\n- MySQL 데이터베이스 스키마 설계\n- JUnit을 활용한 단위 테스트 작성\n- Git을 통한 협업")
                .skillsLearned("Spring Boot, JPA, MySQL, Git, REST API, Agile")
                .certificateUrl("https://example.com/certificates/internship-2024")
                .isVerified(true)
                .credits(150)
                .verifiedBy(1) // 관리자
                .verificationDate(LocalDate.now().minusMonths(1).atStartOfDay())
                .isPortfolioLinked(false)
                .build();
        employmentRepository.save(internship);
        log.info("테스트 데이터 생성: 승인된 인턴십 - 150점");

        // 2. 승인 대기 중인 현장실습 (3개월)
        ExternalEmployment fieldTraining = ExternalEmployment.builder()
                .userId(2) // 이영희
                .employmentType(EmploymentType.FIELD_TRAINING)
                .companyName("스마트솔루션 주식회사")
                .position("프론트엔드 개발 실습생")
                .department("UX개발팀")
                .startDate(LocalDate.now().minusMonths(3))
                .endDate(LocalDate.now())
                .description("React 기반 웹 애플리케이션 개발 현장실습에 참여했습니다.")
                .workContent("- React 컴포넌트 개발\n- Redux를 통한 상태 관리\n- Figma 디자인 시안을 코드로 구현\n- Storybook 문서화")
                .skillsLearned("React, Redux, TypeScript, Storybook, Figma")
                .certificateUrl("https://example.com/certificates/field-training-2025")
                .isVerified(false)
                .credits(0)
                .isPortfolioLinked(false)
                .build();
        employmentRepository.save(fieldTraining);
        log.info("테스트 데이터 생성: 승인 대기 중인 현장실습");

        // 3. 진행 중인 외부 프로젝트
        ExternalEmployment project = ExternalEmployment.builder()
                .userId(2) // 이영희
                .employmentType(EmploymentType.PROJECT)
                .companyName("푸름대학교 산학협력단")
                .position("프로젝트 참여 연구원")
                .department("AI연구소")
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(null) // 진행 중
                .description("AI 기반 학습 추천 시스템 개발 프로젝트에 참여 중입니다.")
                .workContent("- 사용자 행동 데이터 분석\n- 머신러닝 모델 학습 및 평가\n- Python Flask 기반 API 서버 개발")
                .skillsLearned("Python, TensorFlow, Flask, Pandas, Scikit-learn")
                .isVerified(false)
                .credits(0)
                .isPortfolioLinked(false)
                .build();
        employmentRepository.save(project);
        log.info("테스트 데이터 생성: 진행 중인 외부 프로젝트");

        // 4. 승인된 단기 인턴십 (2개월, 30점)
        ExternalEmployment shortInternship = ExternalEmployment.builder()
                .userId(2) // 이영희
                .employmentType(EmploymentType.INTERNSHIP)
                .companyName("스타트업 코딩스쿨")
                .position("교육 조교")
                .startDate(LocalDate.now().minusYears(1).minusMonths(2))
                .endDate(LocalDate.now().minusYears(1))
                .description("초등학생 대상 코딩 교육 보조 업무를 수행했습니다.")
                .workContent("- Scratch 프로그래밍 교육 보조\n- 학생 과제 피드백\n- 교육 자료 준비")
                .skillsLearned("교육 기획, Scratch, 커뮤니케이션")
                .isVerified(true)
                .credits(30)
                .verifiedBy(1)
                .verificationDate(LocalDate.now().minusYears(1).plusDays(5).atStartOfDay())
                .isPortfolioLinked(false)
                .build();
        employmentRepository.save(shortInternship);
        log.info("테스트 데이터 생성: 승인된 단기 인턴십 - 30점");

        // 5. 거절된 활동 (증빙 자료 부족)
        ExternalEmployment rejected = ExternalEmployment.builder()
                .userId(2) // 이영희
                .employmentType(EmploymentType.PROJECT)
                .companyName("프리랜서 프로젝트")
                .position("웹 개발자")
                .startDate(LocalDate.now().minusMonths(4))
                .endDate(LocalDate.now().minusMonths(3))
                .description("개인 프리랜서 웹사이트 제작 프로젝트")
                .workContent("- WordPress 기반 웹사이트 제작")
                .isVerified(false)
                .credits(0)
                .verifiedBy(1)
                .verificationDate(LocalDate.now().minusMonths(3).plusDays(10).atStartOfDay())
                .rejectionReason("증빙 자료가 부족합니다. 계약서 또는 완료 확인서를 제출해주세요.")
                .isPortfolioLinked(false)
                .build();
        employmentRepository.save(rejected);
        log.info("테스트 데이터 생성: 거절된 활동 (증빙 부족)");

        log.info("이영희 학생의 총 획득 가점: {}점",
            employmentService.getTotalCreditsByUserId(2));
    }
}
