
package com.example.bookstore.service;

import com.example.bookstore.common.PageResult;
import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository,
                       BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookDTO> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId).stream()
                .map(bookMapper::toDTO)
                .toList();
    }

    /** 数据库分页：按分类 / 关键词 / 全量查询。 */
    @Transactional(readOnly = true)
    public PageResult<BookDTO> getBooks(Long categoryId, String keyword, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Book> result;
        if (categoryId != null) {
            result = bookRepository.findByCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            result = bookRepository.findByNameContainingOrAuthorContainingOrIsbnContaining(
                    keyword, keyword, keyword, pageable);
        } else {
            result = bookRepository.findAll(pageable);
        }
        return PageResult.of(result, bookMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value()));
        return bookMapper.toDTO(book);
    }

    @Transactional(readOnly = true)
    public List<BookDTO> searchBooks(String keyword) {
        return bookRepository.findByNameContainingOrAuthorContainingOrIsbnContaining(keyword, keyword, keyword).stream()
                .map(bookMapper::toDTO)
                .toList();
    }

    @Transactional
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
        return bookMapper.toDTO(savedBook);
    }

    @Transactional
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
        return bookMapper.toDTO(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new CustomException("书籍不存在", HttpStatus.NOT_FOUND.value());
        }
        bookRepository.deleteById(id);
    }

    }
