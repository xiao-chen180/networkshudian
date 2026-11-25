package com.example.bookstore.repository;

import com.example.bookstore.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findByUserIdAndCheckInDate(Long userId, LocalDate date);
    List<CheckIn> findByUserIdAndCheckInDateBetweenOrderByCheckInDateDesc(Long userId, LocalDate start, LocalDate end);
    Optional<CheckIn> findTopByUserIdOrderByCheckInDateDesc(Long userId);
    boolean existsByUserIdAndCheckInDate(Long userId, LocalDate date);
}
