package com.goldtrading.backend.admin.dto.response;

import java.util.List;

public record PagedDataResponse<T>(List<T> items, int page, int pageSize, long totalItems, int totalPages) {
    public static <T> PagedDataResponse<T> of(org.springframework.data.domain.Page<T> pageData) {
        return new PagedDataResponse<>(pageData.getContent(), pageData.getNumber(), pageData.getSize(), pageData.getTotalElements(), pageData.getTotalPages());
    }
}
