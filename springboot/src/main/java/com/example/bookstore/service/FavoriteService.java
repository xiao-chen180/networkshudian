package com.example.bookstore.service;

import com.example.bookstore.dto.FavoriteDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Favorite;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.FavoriteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final BookRepository bookRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, BookRepository bookRepository) {
        this.favoriteRepository = favoriteRepository;
        this.bookRepository = bookRepository;
    }

    public List<FavoriteDTO> getFavoritesByUserId(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(favorite -> convertToDTO(favorite, userId))
                .collect(Collectors.toList());
    }

    public boolean isFavorited(Long userId, Long bookId) {
        return favoriteRepository.existsByUserIdAndBookId(userId, bookId);
    }

    public FavoriteDTO addFavorite(Long userId, Long bookId) {
        // 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new CustomException("已经收藏过此书了", HttpStatus.BAD_REQUEST.value());
        }

        // 检查书籍是否存在
        bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBookId(bookId);

        Favorite savedFavorite = favoriteRepository.save(favorite);
        return convertToDTO(savedFavorite, userId);
    }

    public void removeFavorite(Long userId, Long bookId) {
        if (!favoriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new CustomException("未收藏此书", HttpStatus.BAD_REQUEST.value());
        }
        favoriteRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    private FavoriteDTO convertToDTO(Favorite favorite, Long userId) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(favorite.getId());
        dto.setUserId(favorite.getUserId());
        dto.setBookId(favorite.getBookId());
        dto.setCreatedAt(favorite.getCreatedAt());

        // 获取书籍信息
        bookRepository.findById(favorite.getBookId()).ifPresent(book -> {
            dto.setBookName(book.getName());
            dto.setBookAuthor(book.getAuthor());
            dto.setBookPrice(book.getPrice());
            dto.setBookImageUrl(book.getImageUrl());
            if (book.getCategory() != null) {
                dto.setCategoryName(book.getCategory().getName());
            }
        });

        return dto;
    }
}
