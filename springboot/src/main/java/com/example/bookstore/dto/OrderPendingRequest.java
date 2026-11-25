package com.example.bookstore.dto;

import jakarta.validation.Valid;

import java.util.List;

public record OrderPendingRequest(
        String shippingAddress,
        String phone,
        @Valid List<OrderItemRequest> items,
        Long userCouponId
) {
}