package com.example.bookstore.repository;

import com.example.bookstore.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    List<UserCoupon> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndCouponIdAndStatus(Long userId, Long couponId, String status);
}
