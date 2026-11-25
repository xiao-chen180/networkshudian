package com.example.bookstore.controller;

import com.example.bookstore.dto.CreateReviewRequest;
import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.dto.UpdateReviewRequest;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.ReviewService;
import jakarta.validation.Valid;
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
        return ResponseEntity.ok(reviewService.getReviewsByBookId(bookId));
    }

    @GetMapping("/book/{bookId}/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewStats(bookId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewDTO>> getMyReviews() {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody CreateReviewRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ReviewDTO review = reviewService.createReview(
                userId, request.bookId(), request.rating(), request.content() != null ? request.content() : "");
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateReviewRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(reviewService.updateReview(userId, id, request.rating(), request.content()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reviewService.deleteReview(userId, id);
        return ResponseEntity.noContent().build();
    }
}