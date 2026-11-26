package com.example.bookstore.controller;

import com.example.bookstore.service.BookAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 找书助手：把自然语言需求转成书单。
 * 公开接口，未登录也能用（导购场景）。
 */
@RestController
@RequestMapping("/api/ai")
public class BookAssistantController {

    private final BookAssistantService bookAssistantService;

    public BookAssistantController(BookAssistantService bookAssistantService) {
        this.bookAssistantService = bookAssistantService;
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestBody Map<String, Object> body) {
        String query = body.get("query") == null ? "" : String.valueOf(body.get("query"));
        int limit = 6;
        Object rawLimit = body.get("limit");
        if (rawLimit instanceof Number number) {
            limit = number.intValue();
        }
        return ResponseEntity.ok(bookAssistantService.search(query, limit));
    }
}