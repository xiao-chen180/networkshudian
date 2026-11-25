package com.example.bookstore.controller;

import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUser currentUser;

    public ReviewController(ReviewService reviewService, CurrentUser currentUser) {
        this.reviewService = reviewService;
        this.currentUser = currentUser;
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByBookId(@PathVariable Long bookId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/book/{bookId}/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats(@PathVariable Long bookId) {
        Map<String, Object> stats = reviewService.getReviewStats(bookId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewDTO>> getMyReviews(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long bookId = Long.valueOf(body.get("bookId").toString());
        Integer rating = (Integer) body.get("rating");
        String content = body.get("content") != null ? body.get("content").toString() : "";

        ReviewDTO review = reviewService.createReview(userId, bookId, rating, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer rating = body.get("rating") != null ? (Integer) body.get("rating") : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;

        ReviewDTO review = reviewService.updateReview(userId, id, rating, content);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        reviewService.deleteReview(userId, id);
        return ResponseEntity.noContent().build();
    }
}
