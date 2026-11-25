package com.example.bookstore.controller;

import com.example.bookstore.dto.CheckInDTO;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.CheckInService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;
    private final CurrentUser currentUser;

    public CheckInController(CheckInService checkInService, CurrentUser currentUser) {
        this.checkInService = checkInService;
        this.currentUser = currentUser;
    }

    @GetMapping("/status")
    public ResponseEntity<CheckInDTO> getCheckInStatus(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CheckInDTO status = checkInService.getCheckInStatus(userId);
        return ResponseEntity.ok(status);
    }

    @PostMapping
    public ResponseEntity<CheckInDTO> checkIn(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CheckInDTO status = checkInService.checkIn(userId);
        return ResponseEntity.ok(status);
    }
}
