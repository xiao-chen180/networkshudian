package com.example.bookstore.controller;

import com.example.bookstore.dto.CouponDTO;
import com.example.bookstore.dto.UserCouponDTO;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;
    private final CurrentUser currentUser;

    public CouponController(CouponService couponService, CurrentUser currentUser) {
        this.couponService = couponService;
        this.currentUser = currentUser;
    }

    @GetMapping("/available")
    public ResponseEntity<List<CouponDTO>> getAvailableCoupons() {
        List<CouponDTO> coupons = couponService.getAvailableCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserCouponDTO>> getMyCoupons(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<UserCouponDTO> coupons = couponService.getUserCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/my/unused")
    public ResponseEntity<List<UserCouponDTO>> getMyUnusedCoupons(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<UserCouponDTO> coupons = couponService.getUnusedCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/{couponId}/claim")
    public ResponseEntity<UserCouponDTO> claimCoupon(
            HttpServletRequest request,
            @PathVariable Long couponId) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserCouponDTO coupon = couponService.claimCoupon(userId, couponId);
        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, BigDecimal>> calculateDiscount(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userCouponId = Long.valueOf(body.get("userCouponId").toString());
        BigDecimal totalAmount = new BigDecimal(body.get("totalAmount").toString());

        BigDecimal discount = couponService.calculateDiscount(userCouponId, totalAmount);
        return ResponseEntity.ok(Map.of("discount", discount));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<CouponDTO>> getAllCoupons(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<CouponDTO> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/admin")
    public ResponseEntity<CouponDTO> createCoupon(
            HttpServletRequest request,
            @RequestBody CouponDTO couponDTO) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CouponDTO created = couponService.createCoupon(couponDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
