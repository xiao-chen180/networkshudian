package com.example.bookstore.controller;

import com.example.bookstore.dto.AfterSaleDTO;
import com.example.bookstore.dto.AfterSaleHandleRequest;
import com.example.bookstore.service.AfterSaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 后台售后管理（/api/admin/** 已在 SecurityConfig 中要求 ADMIN 角色） */
@RestController
@RequestMapping("/api/admin/after-sales")
public class AdminAfterSaleController {

    private final AfterSaleService afterSaleService;

    public AdminAfterSaleController(AfterSaleService afterSaleService) {
        this.afterSaleService = afterSaleService;
    }

    @GetMapping
    public ResponseEntity<List<AfterSaleDTO>> list() {
        return ResponseEntity.ok(afterSaleService.getAll());
    }

    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Long>> pendingCount() {
        return ResponseEntity.ok(Map.of("count", afterSaleService.countPending()));
    }

    @PutMapping("/{id}/handle")
    public ResponseEntity<AfterSaleDTO> handle(@PathVariable Long id,
                                               @RequestBody AfterSaleHandleRequest request) {
        return ResponseEntity.ok(afterSaleService.handle(id, request.status(), request.adminRemark()));
    }
}