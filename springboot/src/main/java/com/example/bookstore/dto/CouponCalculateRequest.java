package com.example.bookstore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CouponCalculateRequest(
        @NotNull(message = "优惠券不能为空") Long userCouponId,
        @NotNull(message = "订单金额不能为空")
        @DecimalMin(value = "0", message = "订单金额不能为负") BigDecimal totalAmount
) {
}