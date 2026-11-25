package com.example.bookstore.controller;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final BookRepository bookRepository;

    public RankingController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/sales")
    public ResponseEntity<List<BookDTO>> getSalesRanking() {
        List<Book> allBooks = bookRepository.findAll();
        List<BookDTO> books = allBooks.stream()
                .sorted(Comparator.comparingInt((Book b) -> b.getSalesCount() != null ? b.getSalesCount() : 0).reversed())
                .limit(20)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(books);
    }

    @GetMapping("/rating")
    public ResponseEntity<List<BookDTO>> getRatingRanking() {
        List<Book> allBooks = bookRepository.findAll();
        List<BookDTO> books = allBooks.stream()
                .filter(b -> b.getReviewCount() != null && b.getReviewCount() > 0)
                .sorted(Comparator.comparing((Book b) -> b.getAvgRating() != null ? b.getAvgRating() : BigDecimal.ZERO).reversed())
                .limit(20)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(books);
    }

    @GetMapping("/new")
    public ResponseEntity<List<BookDTO>> getNewRanking() {
        List<Book> allBooks = bookRepository.findAll();
        List<BookDTO> books = allBooks.stream()
                .sorted(Comparator.comparing((Book b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN).reversed())
                .limit(20)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(books);
    }

    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setName(book.getName());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPages(book.getPages());
        dto.setPrice(book.getPrice());
        dto.setIsbn(book.getIsbn());
        dto.setStock(book.getStock());
        dto.setImageUrl(book.getImageUrl());
        dto.setAvgRating(book.getAvgRating());
        dto.setReviewCount(book.getReviewCount());
        dto.setSalesCount(book.getSalesCount());
        if (book.getCategory() != null) {
            dto.setCategoryId(book.getCategory().getId());
            dto.setCategoryName(book.getCategory().getName());
        }
        return dto;
    }
}
