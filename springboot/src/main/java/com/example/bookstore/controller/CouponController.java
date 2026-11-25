package com.example.bookstore.controller;

import com.example.bookstore.dto.CouponDTO;
import com.example.bookstore.dto.UserCouponDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public CouponController(CouponService couponService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.couponService = couponService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                String account = jwtUtil.extractUsername(token);
                User user = userRepository.findByAccount(account).orElse(null);
                if (user != null) return user.getId();
            } catch (Exception e) {}
        }
        return null;
    }

    @GetMapping("/available")
    public ResponseEntity<List<CouponDTO>> getAvailableCoupons() {
        List<CouponDTO> coupons = couponService.getAvailableCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserCouponDTO>> getMyCoupons(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<UserCouponDTO> coupons = couponService.getUserCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/my/unused")
    public ResponseEntity<List<UserCouponDTO>> getMyUnusedCoupons(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
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
        Long userId = getUserIdFromRequest(request);
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
        Long userId = getUserIdFromRequest(request);
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
        Long userId = getUserIdFromRequest(request);
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
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CouponDTO created = couponService.createCoupon(couponDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
