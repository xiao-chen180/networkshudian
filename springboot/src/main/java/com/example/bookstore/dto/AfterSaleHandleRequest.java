package com.example.bookstore.dto;

/** 管理员处理售后：APPROVED 通过 / REJECTED 驳回 / COMPLETED 完成 */
public record AfterSaleHandleRequest(
        String status,
        String adminRemark
) {
}