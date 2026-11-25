package com.example.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PriceUpdateRequest(
        Long categoryId,
        @NotNull(message = "调整值不能为空") BigDecimal adjustment,
        @Pattern(regexp = "fixed|percent", message = "调整方式只能是 fixed 或 percent") String type
) {
}