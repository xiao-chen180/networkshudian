package com.example.bookstore.controller;

import com.example.bookstore.dto.MessageDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtUtil;
import com.example.bookstore.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public MessageController(MessageService messageService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                String account = jwtUtil.extractUsername(authorization.substring(7));
                User user = userRepository.findByAccount(account).orElse(null);
                if (user != null) return user.getId();
            } catch (Exception e) {}
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<MessageDTO>> getMessages(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(messageService.getMessages(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(messageService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        messageService.markAsRead(userId, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        messageService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
