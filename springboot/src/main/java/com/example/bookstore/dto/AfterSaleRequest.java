package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 用户提交售后申请 */
public record AfterSaleRequest(
        @NotNull(message = "订单不能为空") Long orderId,
        @NotBlank(message = "请选择售后类型") String type,
        String reason,
        String description
) {
}