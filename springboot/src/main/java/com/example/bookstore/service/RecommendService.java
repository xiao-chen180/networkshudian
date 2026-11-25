package com.example.bookstore.service;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.repository.BookRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommendService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public RecommendService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional(readOnly = true)
    public List<BookDTO> guessYouLike() {
        return bookRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "salesCount")))
                .getContent().stream()
                .map(bookMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookDTO> sameCategory(Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null || book.getCategory() == null) {
            return List.of();
        }
        return bookRepository.findByCategoryId(book.getCategory().getId(), PageRequest.of(0, 7))
                .getContent().stream()
                .filter(b -> !b.getId().equals(bookId))
                .limit(6)
                .map(bookMapper::toDTO)
                .toList();
    }
}