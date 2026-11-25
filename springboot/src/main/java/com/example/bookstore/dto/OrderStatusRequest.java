package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusRequest(
        @NotBlank(message = "订单状态不能为空") String status
) {
}