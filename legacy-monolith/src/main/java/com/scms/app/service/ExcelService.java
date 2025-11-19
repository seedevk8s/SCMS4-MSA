package com.scms.app.service;

import com.scms.app.model.ProgramApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel 파일 생성 서비스
 */
@Service
@Slf4j
public class ExcelService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 프로그램 신청 목록을 Excel 파일로 생성
     */
    public ByteArrayInputStream generateApplicationsExcel(List<ProgramApplication> applications, String programTitle) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("신청 목록");

            // 헤더 스타일
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 헤더 행 생성
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "신청 ID", "학번", "이름", "전화번호", "이메일",
                    "학과", "학년", "상태", "신청일", "승인일", "완료일", "거부일", "취소일", "거부 사유"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 행 생성
            int rowNum = 1;
            for (ProgramApplication app : applications) {
                Row row = sheet.createRow(rowNum++);

                // 신청 ID
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(app.getApplicationId());
                cell0.setCellStyle(dataStyle);

                // 학번
                Cell cell1 = row.createCell(1);
                if (app.getUser().getStudentNum() != null) {
                    cell1.setCellValue(app.getUser().getStudentNum());
                }
                cell1.setCellStyle(dataStyle);

                // 이름
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(app.getUser().getName());
                cell2.setCellStyle(dataStyle);

                // 전화번호
                Cell cell3 = row.createCell(3);
                if (app.getUser().getPhone() != null) {
                    cell3.setCellValue(app.getUser().getPhone());
                }
                cell3.setCellStyle(dataStyle);

                // 이메일
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(app.getUser().getEmail());
                cell4.setCellStyle(dataStyle);

                // 학과
                Cell cell5 = row.createCell(5);
                if (app.getUser().getDepartment() != null) {
                    cell5.setCellValue(app.getUser().getDepartment());
                }
                cell5.setCellStyle(dataStyle);

                // 학년
                Cell cell6 = row.createCell(6);
                if (app.getUser().getGrade() != null) {
                    cell6.setCellValue(app.getUser().getGrade());
                }
                cell6.setCellStyle(dataStyle);

                // 상태
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(getStatusText(app.getStatus().name()));
                cell7.setCellStyle(dataStyle);

                // 신청일
                Cell cell8 = row.createCell(8);
                if (app.getAppliedAt() != null) {
                    cell8.setCellValue(app.getAppliedAt().format(DATE_FORMATTER));
                }
                cell8.setCellStyle(dataStyle);

                // 승인일
                Cell cell9 = row.createCell(9);
                if (app.getApprovedAt() != null) {
                    cell9.setCellValue(app.getApprovedAt().format(DATE_FORMATTER));
                }
                cell9.setCellStyle(dataStyle);

                // 완료일
                Cell cell10 = row.createCell(10);
                if (app.getCompletedAt() != null) {
                    cell10.setCellValue(app.getCompletedAt().format(DATE_FORMATTER));
                }
                cell10.setCellStyle(dataStyle);

                // 거부일
                Cell cell11 = row.createCell(11);
                if (app.getRejectedAt() != null) {
                    cell11.setCellValue(app.getRejectedAt().format(DATE_FORMATTER));
                }
                cell11.setCellStyle(dataStyle);

                // 취소일
                Cell cell12 = row.createCell(12);
                if (app.getCancelledAt() != null) {
                    cell12.setCellValue(app.getCancelledAt().format(DATE_FORMATTER));
                }
                cell12.setCellStyle(dataStyle);

                // 거부 사유
                Cell cell13 = row.createCell(13);
                if (app.getRejectionReason() != null) {
                    cell13.setCellValue(app.getRejectionReason());
                }
                cell13.setCellStyle(dataStyle);
            }

            // 열 너비 자동 조정
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                // 최소 너비 설정 (한글 지원)
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3000));
            }

            workbook.write(out);
            log.info("Excel 파일 생성 완료: 프로그램={}, 신청 수={}", programTitle, applications.size());
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Excel 파일 생성 실패", e);
            throw new RuntimeException("Excel 파일을 생성할 수 없습니다.", e);
        }
    }

    /**
     * 헤더 스타일 생성
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 배경색
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 테두리
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 정렬
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 폰트
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        return style;
    }

    /**
     * 데이터 스타일 생성
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 테두리
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 정렬
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 자동 줄바꿈
        style.setWrapText(true);

        return style;
    }

    /**
     * 상태 코드를 한글 텍스트로 변환
     */
    private String getStatusText(String status) {
        switch (status) {
            case "PENDING":
                return "대기 중";
            case "APPROVED":
                return "승인됨";
            case "COMPLETED":
                return "참여 완료";
            case "REJECTED":
                return "거부됨";
            case "CANCELLED":
                return "취소됨";
            default:
                return status;
        }
    }
}
