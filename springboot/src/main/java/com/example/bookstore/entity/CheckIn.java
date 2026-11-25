package com.example.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "check_in_date"})
})
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "consecutive_days")
    private Integer consecutiveDays = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public CheckIn() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
