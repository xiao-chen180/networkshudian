package com.example.bookstore.service;

import com.example.bookstore.dto.CouponDTO;
import com.example.bookstore.dto.UserCouponDTO;
import com.example.bookstore.entity.Coupon;
import com.example.bookstore.entity.UserCoupon;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.CouponRepository;
import com.example.bookstore.repository.UserCouponRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    public CouponService(CouponRepository couponRepository, UserCouponRepository userCouponRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
    }

    public List<CouponDTO> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByStatus("ACTIVE").stream()
                .filter(c -> c.getEndTime() == null || c.getEndTime().isAfter(now))
                .filter(c -> c.getUsedNum() < c.getTotalNum())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserCouponDTO> getUserCoupons(Long userId) {
        return convertAll(userCouponRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    public List<UserCouponDTO> getUnusedCoupons(Long userId) {
        return convertAll(userCouponRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "UNUSED"));
    }

    private List<UserCouponDTO> convertAll(List<UserCoupon> userCoupons) {
        if (userCoupons.isEmpty()) {
            return List.of();
        }
        Map<Long, Coupon> couponMap = couponRepository.findAllById(
                        userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));
        return userCoupons.stream()
                .map(uc -> convertToUserCouponDTO(uc, couponMap.get(uc.getCouponId())))
                .collect(Collectors.toList());
    }

    public UserCouponDTO claimCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException("优惠券不存在", HttpStatus.NOT_FOUND.value()));

        if (!"ACTIVE".equals(coupon.getStatus())) {
            throw new CustomException("优惠券已失效", HttpStatus.BAD_REQUEST.value());
        }

        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("优惠券已过期", HttpStatus.BAD_REQUEST.value());
        }

        if (coupon.getUsedNum() >= coupon.getTotalNum()) {
            throw new CustomException("优惠券已领完", HttpStatus.BAD_REQUEST.value());
        }

        if (userCouponRepository.existsByUserIdAndCouponIdAndStatus(userId, couponId, "UNUSED")) {
            throw new CustomException("您已领取过此优惠券", HttpStatus.BAD_REQUEST.value());
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus("UNUSED");

        coupon.setUsedNum(coupon.getUsedNum() + 1);
        couponRepository.save(coupon);

        UserCoupon saved = userCouponRepository.save(userCoupon);
        return convertToUserCouponDTO(saved, coupon);
    }

    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal totalAmount) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CustomException("优惠券不存在", HttpStatus.NOT_FOUND.value()));

        if (!"UNUSED".equals(userCoupon.getStatus())) {
            throw new CustomException("优惠券已使用", HttpStatus.BAD_REQUEST.value());
        }

        Coupon coupon = couponRepository.findById(userCoupon.getCouponId())
                .orElseThrow(() -> new CustomException("优惠券信息不存在", HttpStatus.NOT_FOUND.value()));

        if (coupon.getMinAmount() != null && totalAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new CustomException("未达到最低消费金额", HttpStatus.BAD_REQUEST.value());
        }

        switch (coupon.getType()) {
            case "FIXED":
                return coupon.getValue();
            case "PERCENT":
                return totalAmount.multiply(coupon.getValue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            case "MONEY_OFF":
                return coupon.getValue();
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 下单时核销优惠券：校验归属与可用性，计算抵扣金额并把券置为已使用。
     * 返回抵扣金额；非法情况抛异常由调用方事务回滚。
     */
    @Transactional
    public BigDecimal useCoupon(Long userId, Long userCouponId, BigDecimal totalAmount) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CustomException("优惠券不存在", HttpStatus.NOT_FOUND.value()));
        if (userCoupon.getUserId() == null || !userCoupon.getUserId().equals(userId)) {
            throw new CustomException("优惠券不属于当前用户", HttpStatus.FORBIDDEN.value());
        }

        BigDecimal discount = calculateDiscount(userCouponId, totalAmount);

        userCoupon.setStatus("USED");
        userCouponRepository.save(userCoupon);
        return discount;
    }

    /** 取消订单时退回优惠券 */
    @Transactional
    public void restoreCoupon(Long userId, Long userCouponId) {
        userCouponRepository.findById(userCouponId).ifPresent(userCoupon -> {
            if (userId == null || userId.equals(userCoupon.getUserId())) {
                userCoupon.setStatus("UNUSED");
                userCouponRepository.save(userCoupon);
            }
        });
    }

    public CouponDTO createCoupon(CouponDTO dto) {
        Coupon coupon = new Coupon();
        coupon.setName(dto.getName());
        coupon.setType(dto.getType());
        coupon.setValue(dto.getValue());
        coupon.setMinAmount(dto.getMinAmount() != null ? dto.getMinAmount() : BigDecimal.ZERO);
        coupon.setTotalNum(dto.getTotalNum() != null ? dto.getTotalNum() : 100);
        coupon.setStartTime(dto.getStartTime());
        coupon.setEndTime(dto.getEndTime());
        coupon.setStatus("ACTIVE");
        Coupon saved = couponRepository.save(coupon);
        return convertToDTO(saved);
    }

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CouponDTO convertToDTO(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        dto.setId(coupon.getId());
        dto.setName(coupon.getName());
        dto.setType(coupon.getType());
        dto.setValue(coupon.getValue());
        dto.setMinAmount(coupon.getMinAmount());
        dto.setTotalNum(coupon.getTotalNum());
        dto.setUsedNum(coupon.getUsedNum());
        dto.setRemainNum(coupon.getTotalNum() - coupon.getUsedNum());
        dto.setStartTime(coupon.getStartTime());
        dto.setEndTime(coupon.getEndTime());
        dto.setStatus(coupon.getStatus());
        return dto;
    }

    private UserCouponDTO convertToUserCouponDTO(UserCoupon userCoupon, Coupon coupon) {
        UserCouponDTO dto = new UserCouponDTO();
        dto.setId(userCoupon.getId());
        dto.setCouponId(userCoupon.getCouponId());
        dto.setStatus(userCoupon.getStatus());
        dto.setCreatedAt(userCoupon.getCreatedAt());

        if (coupon != null) {
            dto.setCouponName(coupon.getName());
            dto.setType(coupon.getType());
            dto.setValue(coupon.getValue());
            dto.setMinAmount(coupon.getMinAmount());
            dto.setStartTime(coupon.getStartTime());
            dto.setEndTime(coupon.getEndTime());
        }

        return dto;
    }
}
