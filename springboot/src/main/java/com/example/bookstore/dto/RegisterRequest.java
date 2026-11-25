package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;

    private String email;

    private String phone;

    private String adminSecret;

    public RegisterRequest() {}

    public RegisterRequest(String account, String username, String password, String email, String phone) {
        this.account = account;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAdminSecret() { return adminSecret; }
    public void setAdminSecret(String adminSecret) { this.adminSecret = adminSecret; }
}