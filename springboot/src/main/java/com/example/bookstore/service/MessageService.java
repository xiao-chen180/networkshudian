package com.example.bookstore.service;

import com.example.bookstore.dto.MessageDTO;
import com.example.bookstore.entity.Message;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<MessageDTO> getMessages(Long userId) {
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public Map<String, Long> getUnreadCount(Long userId) {
        long count = messageRepository.countByUserIdAndIsRead(userId, false);
        return Map.of("count", count);
    }

    public void markAsRead(Long userId, Long messageId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException("消息不存在", HttpStatus.NOT_FOUND.value()));
        msg.setIsRead(true);
        messageRepository.save(msg);
    }

    public void markAllAsRead(Long userId) {
        messageRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(m -> !m.getIsRead())
                .forEach(m -> { m.setIsRead(true); messageRepository.save(m); });
    }

    public void sendMessage(Long userId, String title, String content, String type) {
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(type != null ? type : "SYSTEM");
        messageRepository.save(msg);
    }

    private MessageDTO convertToDTO(Message msg) {
        MessageDTO dto = new MessageDTO();
        dto.setId(msg.getId());
        dto.setTitle(msg.getTitle());
        dto.setContent(msg.getContent());
        dto.setType(msg.getType());
        dto.setIsRead(msg.getIsRead());
        dto.setCreatedAt(msg.getCreatedAt());
        return dto;
    }
}
