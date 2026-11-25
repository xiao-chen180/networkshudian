package com.example.bookstore.service;

import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Review;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.ReviewRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<ReviewDTO> getReviewsByBookId(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewDTO> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getReviewStats(Long bookId) {
        Map<String, Object> stats = new HashMap<>();
        Double avgRating = reviewRepository.findAvgRatingByBookId(bookId);
        long reviewCount = reviewRepository.countByBookId(bookId);
        List<Object[]> distribution = reviewRepository.findRatingDistributionByBookId(bookId);

        stats.put("avgRating", avgRating != null ? BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0);
        stats.put("reviewCount", reviewCount);

        // 初始化评分分布
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(rating, count);
        }
        stats.put("ratingDistribution", ratingDistribution);

        return stats;
    }

    public ReviewDTO createReview(Long userId, Long bookId, Integer rating, String content) {
        // 检查是否已评论
        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new CustomException("您已评论过此书，不能重复评论", HttpStatus.BAD_REQUEST.value());
        }

        // 检查评分范围
        if (rating < 1 || rating > 5) {
            throw new CustomException("评分必须在1-5之间", HttpStatus.BAD_REQUEST.value());
        }

        // 检查书籍是否存在
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));

        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在", HttpStatus.NOT_FOUND.value()));

        Review review = new Review();
        review.setUserId(userId);
        review.setBookId(bookId);
        review.setRating(rating);
        review.setContent(content);

        Review savedReview = reviewRepository.save(review);
        updateBookReviewStats(bookId);
        return convertToDTO(savedReview);
    }

    public ReviewDTO updateReview(Long userId, Long reviewId, Integer rating, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("评论不存在", HttpStatus.NOT_FOUND.value()));

        if (!review.getUserId().equals(userId)) {
            throw new CustomException("只能修改自己的评论", HttpStatus.FORBIDDEN.value());
        }

        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new CustomException("评分必须在1-5之间", HttpStatus.BAD_REQUEST.value());
            }
            review.setRating(rating);
        }
        if (content != null) {
            review.setContent(content);
        }

        Review updatedReview = reviewRepository.save(review);
        updateBookReviewStats(review.getBookId());
        return convertToDTO(updatedReview);
    }

    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("评论不存在", HttpStatus.NOT_FOUND.value()));

        if (!review.getUserId().equals(userId)) {
            throw new CustomException("只能删除自己的评论", HttpStatus.FORBIDDEN.value());
        }

        Long bookId = review.getBookId();
        reviewRepository.deleteById(reviewId);
        updateBookReviewStats(bookId);
    }

    private void updateBookReviewStats(Long bookId) {
        bookRepository.findById(bookId).ifPresent(book -> {
            Double avgRating = reviewRepository.findAvgRatingByBookId(bookId);
            long reviewCount = reviewRepository.countByBookId(bookId);
            book.setAvgRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            book.setReviewCount((int) reviewCount);
            bookRepository.save(book);
        });
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setBookId(review.getBookId());
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        // 获取用户名
        userRepository.findById(review.getUserId()).ifPresent(user -> {
            dto.setUsername(user.getUsername());
        });

        // 获取书名
        bookRepository.findById(review.getBookId()).ifPresent(book -> {
            dto.setBookName(book.getName());
        });

        return dto;
    }
}
