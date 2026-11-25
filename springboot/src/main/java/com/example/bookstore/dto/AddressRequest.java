package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "收件人不能为空") @Size(max = 50) String name,
        @NotBlank(message = "手机号不能为空") @Size(max = 20) String phone,
        @NotBlank(message = "省份不能为空") String province,
        @NotBlank(message = "城市不能为空") String city,
        String district,
        @NotBlank(message = "详细地址不能为空") String detail,
        Boolean isDefault
) {
}