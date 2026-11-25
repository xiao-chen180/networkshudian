package com.example.bookstore.service;

import com.example.bookstore.dto.PointsLogDTO;
import com.example.bookstore.entity.PointsLog;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.PointsLogRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointsService {

    private final PointsLogRepository pointsLogRepository;
    private final UserRepository userRepository;

    public PointsService(PointsLogRepository pointsLogRepository, UserRepository userRepository) {
        this.pointsLogRepository = pointsLogRepository;
        this.userRepository = userRepository;
    }

    public Integer getUserPoints(Long userId) {
        Integer earned = pointsLogRepository.getTotalEarnedPoints(userId);
        Integer spent = pointsLogRepository.getTotalSpentPoints(userId);
        return earned - spent;
    }

    public List<PointsLogDTO> getPointsLog(Long userId) {
        return pointsLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void addPoints(Long userId, Integer amount, String description) {
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setAmount(amount);
        log.setType("EARN");
        log.setDescription(description);
        pointsLogRepository.save(log);
    }

    public void spendPoints(Long userId, Integer amount, String description) {
        Integer currentPoints = getUserPoints(userId);
        if (currentPoints < amount) {
            throw new CustomException("积分不足", HttpStatus.BAD_REQUEST.value());
        }

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setAmount(amount);
        log.setType("SPEND");
        log.setDescription(description);
        pointsLogRepository.save(log);
    }

    private PointsLogDTO convertToDTO(PointsLog log) {
        PointsLogDTO dto = new PointsLogDTO();
        dto.setId(log.getId());
        dto.setAmount(log.getAmount());
        dto.setType(log.getType());
        dto.setDescription(log.getDescription());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
