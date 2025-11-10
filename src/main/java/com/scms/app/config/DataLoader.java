package com.scms.app.config;

import com.scms.app.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 애플리케이션 시작 시 초기 데이터를 로드하는 클래스
 * 한 번만 실행되도록 프로그램 개수를 확인 후 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ProgramRepository programRepository;

    @Override
    public void run(String... args) throws Exception {
        // 이미 데이터가 있는지 확인
        long count = programRepository.count();

        if (count >= 50) {
            log.info("프로그램 데이터가 이미 존재합니다. ({}개) - 초기 데이터 로드를 건너뜁니다.", count);
            return;
        }

        log.info("초기 프로그램 데이터를 로드합니다...");

        try {
            // data.sql 파일 읽기
            ClassPathResource resource = new ClassPathResource("data.sql");
            String sql = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

            // SQL을 개별 INSERT 문으로 분리
            String[] statements = sql.split(";");

            int insertCount = 0;
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    try {
                        jdbcTemplate.execute(trimmed);
                        insertCount++;
                    } catch (Exception e) {
                        log.warn("SQL 실행 실패 (건너뜀): {}", e.getMessage());
                    }
                }
            }

            long afterCount = programRepository.count();
            log.info("초기 데이터 로드 완료: {}개의 INSERT 문 실행, 총 {}개 프로그램", insertCount, afterCount);

        } catch (Exception e) {
            log.error("초기 데이터 로드 중 오류 발생", e);
        }
    }
}
