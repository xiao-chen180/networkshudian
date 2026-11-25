package com.example.bookstore.controller;

import com.example.bookstore.dto.CheckInDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtUtil;
import com.example.bookstore.service.CheckInService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public CheckInController(CheckInService checkInService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.checkInService = checkInService;
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

    @GetMapping("/status")
    public ResponseEntity<CheckInDTO> getCheckInStatus(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CheckInDTO status = checkInService.getCheckInStatus(userId);
        return ResponseEntity.ok(status);
    }

    @PostMapping
    public ResponseEntity<CheckInDTO> checkIn(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CheckInDTO status = checkInService.checkIn(userId);
        return ResponseEntity.ok(status);
    }
}
