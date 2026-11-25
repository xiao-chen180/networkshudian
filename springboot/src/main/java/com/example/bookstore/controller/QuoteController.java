package com.example.bookstore.controller;

import com.example.bookstore.entity.Quote;
import com.example.bookstore.repository.QuoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteRepository quoteRepository;

    public QuoteController(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @GetMapping("/random")
    public ResponseEntity<Quote> getRandomQuote() {
        List<Quote> all = quoteRepository.findAll();
        if (all.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(all.get(new Random().nextInt(all.size())));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Quote>> getByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(quoteRepository.findByBookId(bookId));
    }
}
