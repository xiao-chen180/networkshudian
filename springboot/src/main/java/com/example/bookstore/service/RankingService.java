package com.example.bookstore.service;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RankingService {

    private static final int LIMIT = 20;

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public RankingService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional(readOnly = true)
    public List<BookDTO> getSalesRanking() {
        return toDTOs(bookRepository.findAll(PageRequest.of(0, LIMIT, Sort.by(Sort.Direction.DESC, "salesCount"))));
    }

    @Transactional(readOnly = true)
    public List<BookDTO> getRatingRanking() {
        return toDTOs(bookRepository.findByReviewCountGreaterThan(0,
                PageRequest.of(0, LIMIT, Sort.by(Sort.Direction.DESC, "avgRating"))));
    }

    @Transactional(readOnly = true)
    public List<BookDTO> getNewRanking() {
        return toDTOs(bookRepository.findAll(PageRequest.of(0, LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    private List<BookDTO> toDTOs(Page<Book> page) {
        return page.getContent().stream().map(bookMapper::toDTO).toList();
    }
}