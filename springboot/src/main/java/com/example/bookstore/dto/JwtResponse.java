package com.example.bookstore.dto;

import java.math.BigDecimal;

public class JwtResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String account;
    private String username;
    private String email;
    private BigDecimal discount;
    private String role;

    public JwtResponse() {}

    public JwtResponse(String accessToken, Long userId, String account, String username, String email, BigDecimal discount) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.account = account;
        this.username = username;
        this.email = email;
        this.discount = discount;
    }

    public JwtResponse(String accessToken, Long userId, String account, String username, String email, BigDecimal discount, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.account = account;
        this.username = username;
        this.email = email;
        this.discount = discount;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}