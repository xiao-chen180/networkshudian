package com.example.bookstore.dto;

public record AddressDTO(
        Long id,
        String name,
        String phone,
        String province,
        String city,
        String district,
        String detail,
        Boolean isDefault
) {
}