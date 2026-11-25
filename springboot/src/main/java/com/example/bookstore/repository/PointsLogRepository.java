package com.example.bookstore.repository;

import com.example.bookstore.entity.PointsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsLogRepository extends JpaRepository<PointsLog, Long> {
    List<PointsLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PointsLog p WHERE p.userId = :userId AND p.type = 'EARN'")
    Integer getTotalEarnedPoints(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PointsLog p WHERE p.userId = :userId AND p.type = 'SPEND'")
    Integer getTotalSpentPoints(@Param("userId") Long userId);
}
