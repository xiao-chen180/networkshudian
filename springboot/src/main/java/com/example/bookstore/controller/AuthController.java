
package com.example.bookstore.controller;

import com.example.bookstore.dto.ChangePasswordRequest;
import com.example.bookstore.dto.JwtResponse;
import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.dto.UpdateUserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        JwtResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<UserResponse> getUser() {
        UserResponse response = userService.getUser();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
