package com.example.bookstore.controller;

import com.example.bookstore.dto.QuoteDTO;
import com.example.bookstore.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/random")
    public ResponseEntity<QuoteDTO> getRandomQuote() {
        return quoteService.getRandomQuote()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<QuoteDTO>> getByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(quoteService.getByBook(bookId));
    }
}