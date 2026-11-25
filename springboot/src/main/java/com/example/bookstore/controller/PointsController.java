package com.example.bookstore.controller;

import com.example.bookstore.dto.PointsLogDTO;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.PointsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService pointsService;
    private final CurrentUser currentUser;

    public PointsController(PointsService pointsService, CurrentUser currentUser) {
        this.pointsService = pointsService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<Map<String, Integer>> getMyPoints(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer points = pointsService.getUserPoints(userId);
        return ResponseEntity.ok(Map.of("points", points));
    }

    @GetMapping("/log")
    public ResponseEntity<List<PointsLogDTO>> getPointsLog(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PointsLogDTO> log = pointsService.getPointsLog(userId);
        return ResponseEntity.ok(log);
    }
}
