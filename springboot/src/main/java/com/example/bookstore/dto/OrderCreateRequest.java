package com.example.bookstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        Long addressId,
        @NotEmpty(message = "订单明细不能为空") @Valid List<OrderItemRequest> items,
        String paymentMethod
) {
}