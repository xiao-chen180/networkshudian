package com.example.bookstore.controller;

import com.example.bookstore.dto.PointsLogDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public PointsController(PointsService pointsService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.pointsService = pointsService;
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

    @GetMapping
    public ResponseEntity<Map<String, Integer>> getMyPoints(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer points = pointsService.getUserPoints(userId);
        return ResponseEntity.ok(Map.of("points", points));
    }

    @GetMapping("/log")
    public ResponseEntity<List<PointsLogDTO>> getPointsLog(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PointsLogDTO> log = pointsService.getPointsLog(userId);
        return ResponseEntity.ok(log);
    }
}
