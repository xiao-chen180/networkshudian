package com.example.bookstore.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** 统一分页响应结构，字段与前端现有约定保持一致。 */
public record PageResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {

    public static <E, T> PageResult<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}