package com.example.bookstore.controller;

import com.example.bookstore.entity.UserBalance;
import com.example.bookstore.security.JwtUtil;
import com.example.bookstore.service.UserBalanceService;
import jakarta.servlet.http.HttpServletRequest;
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
        BigDecimal balance = userBalanceService.getBalance(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recharge")
    public ResponseEntity<Map<String, Object>> recharge(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        BigDecimal amount = new BigDecimal(requestBody.get("amount"));
        UserBalance balance = userBalanceService.recharge(userId, amount);
        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance.getBalance());
        response.put("message", "充值成功");
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