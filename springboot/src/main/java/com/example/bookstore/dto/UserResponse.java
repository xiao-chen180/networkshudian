package com.example.bookstore.dto;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String account;
    private String username;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String account, String username, String email, String phone, LocalDateTime createdAt) {
        this.id = id;
        this.account = account;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
