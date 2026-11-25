package com.example.bookstore.repository;

import com.example.bookstore.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByBookId(Long bookId);
}
