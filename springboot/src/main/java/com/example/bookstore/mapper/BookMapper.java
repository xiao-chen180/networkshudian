package com.example.bookstore.mapper;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookDTO toDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setName(book.getName());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPages(book.getPages());
        dto.setPrice(book.getPrice());
        dto.setIsbn(book.getIsbn());
        dto.setSummary(book.getSummary());
        dto.setCatalog(book.getCatalog());
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