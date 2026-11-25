package com.example.bookstore.controller;

import com.example.bookstore.security.CurrentUser;
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
    private final CurrentUser currentUser;

    public UserBalanceController(UserBalanceService userBalanceService, CurrentUser currentUser) {
        this.userBalanceService = userBalanceService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBalance(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BigDecimal balance = userBalanceService.getBalance(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance);
        return ResponseEntity.ok(response);
    }
}