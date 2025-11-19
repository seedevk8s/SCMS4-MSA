package com.scms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 페이지네이션 응답 DTO
 * 리스트 조회 시 페이징 정보를 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 데이터 리스트
     */
    private List<T> content;

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int pageNumber;

    /**
     * 페이지 크기
     */
    private int pageSize;

    /**
     * 전체 요소 개수
     */
    private long totalElements;

    /**
     * 전체 페이지 개수
     */
    private int totalPages;

    /**
     * 마지막 페이지 여부
     */
    private boolean last;

    /**
     * 첫 페이지 여부
     */
    private boolean first;

    /**
     * 비어있는지 여부
     */
    private boolean empty;

    /**
     * Spring Data의 Page 객체로부터 PageResponse 생성
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .empty(page.isEmpty())
                .build();
    }
}
