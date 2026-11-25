package com.example.bookstore.controller;

import com.example.bookstore.dto.MessageDTO;
import com.example.bookstore.security.CurrentUser;
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
    private final CurrentUser currentUser;

    public MessageController(MessageService messageService, CurrentUser currentUser) {
        this.messageService = messageService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<MessageDTO>> getMessages(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(messageService.getMessages(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(messageService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        messageService.markAsRead(userId, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        messageService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
