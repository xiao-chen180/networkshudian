package com.example.bookstore.controller;

import com.example.bookstore.security.JwtUtil;
import com.example.bookstore.service.UserBalanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/balance")
public class UserBalanceController {

    private final UserBalanceService userBalanceService;
    private final JwtUtil jwtUtil;

    public UserBalanceController(UserBalanceService userBalanceService, JwtUtil jwtUtil) {
        this.userBalanceService = userBalanceService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBalance(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BigDecimal balance = userBalanceService.getBalance(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance);
        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.replace("Bearer ", "");
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}