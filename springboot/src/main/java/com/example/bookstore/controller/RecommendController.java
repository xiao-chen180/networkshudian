package com.example.bookstore.controller;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public RecommendController(BookRepository bookRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/guess-you-like")
    public ResponseEntity<List<BookDTO>> guessYouLike() {
        List<Book> allBooks = bookRepository.findAll();
        List<BookDTO> books = allBooks.stream()
                .sorted(Comparator.comparingInt((Book b) -> b.getSalesCount() != null ? b.getSalesCount() : 0).reversed())
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(books);
    }

    @GetMapping("/same-category/{bookId}")
    public ResponseEntity<List<BookDTO>> sameCategory(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null || book.getCategory() == null) return ResponseEntity.ok(List.of());
        List<BookDTO> books = bookRepository.findByCategoryId(book.getCategory().getId()).stream()
                .filter(b -> !b.getId().equals(bookId))
                .map(this::convertToDTO)
                .limit(6)
                .collect(Collectors.toList());
        return ResponseEntity.ok(books);
    }

    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setName(book.getName());
        dto.setAuthor(book.getAuthor());
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
