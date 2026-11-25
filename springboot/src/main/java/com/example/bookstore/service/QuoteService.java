package com.example.bookstore.service;

import com.example.bookstore.dto.QuoteDTO;
import com.example.bookstore.entity.Quote;
import com.example.bookstore.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;

    public QuoteService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public Optional<QuoteDTO> getRandomQuote() {
        return quoteRepository.findRandom().map(this::toDTO);
    }

    public List<QuoteDTO> getByBook(Long bookId) {
        return quoteRepository.findByBookId(bookId).stream()
                .map(this::toDTO)
                .toList();
    }

    private QuoteDTO toDTO(Quote quote) {
        return new QuoteDTO(
                quote.getId(),
                quote.getBookId(),
                quote.getContent(),
                quote.getAuthorName(),
                quote.getCreatedAt()
        );
    }
}