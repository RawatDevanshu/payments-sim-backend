package com.devh.payment_sim.core;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageResponse<T> {
    private List<T> items;
    private int page;          // 0-based page index
    private int size;          // requested page size
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private String sort;       // e.g., "createdAt,desc;id,desc"

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
            .items(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .sort(page.getSort().toString())
            .build();
    }
}