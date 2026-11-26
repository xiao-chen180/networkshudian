package com.example.bookstore.controller;

import com.example.bookstore.dto.AfterSaleDTO;
import com.example.bookstore.dto.AfterSaleRequest;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.AfterSaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/after-sales")
public class AfterSaleController {

    private final AfterSaleService afterSaleService;
    private final CurrentUser currentUser;

    public AfterSaleController(AfterSaleService afterSaleService, CurrentUser currentUser) {
        this.afterSaleService = afterSaleService;
        this.currentUser = currentUser;
    }

    /** 提交售后申请（仅已付款/已发货/已完成订单） */
    @PostMapping
    public ResponseEntity<AfterSaleDTO> apply(@Valid @RequestBody AfterSaleRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(afterSaleService.apply(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AfterSaleDTO>> getMyAfterSales() {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(afterSaleService.getMyAfterSales(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfterSaleDTO> getById(@PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(afterSaleService.getById(id, userId));
    }

    /** 未处理数量，供个人中心/后台红点使用 */
    @GetMapping("/my/pending-count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        long count = afterSaleService.getMyAfterSales(userId).stream()
                .filter(a -> "PENDING".equals(a.getStatus())).count();
        return ResponseEntity.ok(Map.of("count", count));
    }
}