package com.example.bookstore.controller;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/guess-you-like")
    public ResponseEntity<List<BookDTO>> guessYouLike() {
        return ResponseEntity.ok(recommendService.guessYouLike());
    }

    @GetMapping("/same-category/{bookId}")
    public ResponseEntity<List<BookDTO>> sameCategory(@PathVariable Long bookId) {
        return ResponseEntity.ok(recommendService.sameCategory(bookId));
    }
}