package com.example.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "detail", nullable = false)
    private String detail;

    @Column(name = "is_default")
    private Boolean isDefault = false;
}