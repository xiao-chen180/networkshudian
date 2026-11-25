
package com.example.bookstore.service;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookDTO> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));
        return convertToDTO(book);
    }

    public List<BookDTO> searchBooks(String keyword) {
        return bookRepository.findByNameContainingOrAuthorContainingOrIsbnContaining(keyword, keyword, keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BookDTO createBook(BookDTO bookDTO) {
        if (bookDTO.getIsbn() != null && bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new CustomException("ISBN已存在", HttpStatus.BAD_REQUEST.value());
        }

        Book book = new Book();
        book.setName(bookDTO.getName());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublisher(bookDTO.getPublisher());
        book.setPages(bookDTO.getPages());
        book.setPrice(bookDTO.getPrice());
        book.setIsbn(bookDTO.getIsbn());
        book.setSummary(bookDTO.getSummary());
        book.setCatalog(bookDTO.getCatalog());
        book.setStock(bookDTO.getStock());
        book.setImageUrl(bookDTO.getImageUrl());

        if (bookDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookDTO.getCategoryId())
                    .orElseThrow(() -> new CustomException("分类不存在", HttpStatus.NOT_FOUND.value()));
            book.setCategory(category);
        }

        Book savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }

    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));

        if (bookDTO.getName() != null) book.setName(bookDTO.getName());
        if (bookDTO.getAuthor() != null) book.setAuthor(bookDTO.getAuthor());
        if (bookDTO.getPublisher() != null) book.setPublisher(bookDTO.getPublisher());
        if (bookDTO.getPages() != null) book.setPages(bookDTO.getPages());
        if (bookDTO.getPrice() != null) book.setPrice(bookDTO.getPrice());
        if (bookDTO.getIsbn() != null) {
            if (!book.getIsbn().equals(bookDTO.getIsbn()) && bookRepository.existsByIsbn(bookDTO.getIsbn())) {
                throw new CustomException("ISBN已存在", HttpStatus.BAD_REQUEST.value());
            }
            book.setIsbn(bookDTO.getIsbn());
        }
        if (bookDTO.getSummary() != null) book.setSummary(bookDTO.getSummary());
        if (bookDTO.getCatalog() != null) book.setCatalog(bookDTO.getCatalog());
        if (bookDTO.getStock() != null) book.setStock(bookDTO.getStock());
        if (bookDTO.getImageUrl() != null) book.setImageUrl(bookDTO.getImageUrl());

        if (bookDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookDTO.getCategoryId())
                    .orElseThrow(() -> new CustomException("分类不存在", HttpStatus.NOT_FOUND.value()));
            book.setCategory(category);
        }

        Book updatedBook = bookRepository.save(book);
        return convertToDTO(updatedBook);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value());
        }
        bookRepository.deleteById(id);
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
