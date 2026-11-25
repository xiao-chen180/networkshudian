package com.example.bookstore.repository;

import com.example.bookstore.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    long countByBookId(Long bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookId = :bookId")
    Double findAvgRatingByBookId(@Param("bookId") Long bookId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.bookId = :bookId GROUP BY r.rating")
    List<Object[]> findRatingDistributionByBookId(@Param("bookId") Long bookId);
}
