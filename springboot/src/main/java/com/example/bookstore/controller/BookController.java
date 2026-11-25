
package com.example.bookstore.controller;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBooks(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        List<BookDTO> books;
        if (categoryId != null) {
            books = bookService.getBooksByCategory(categoryId);
        } else if (keyword != null && !keyword.isEmpty()) {
            books = bookService.searchBooks(keyword);
        } else {
            books = bookService.getAllBooks();
        }
        
        int totalElements = books.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<BookDTO> content;
        if (start >= totalElements) {
            content = List.of();
        } else {
            content = books.subList(start, end);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", totalElements);
        response.put("totalPages", (int) Math.ceil((double) totalElements / size));
        response.put("number", page);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    public ResponseEntity<BookDTO> createBook(@RequestBody BookDTO bookDTO) {
        BookDTO createdBook = bookService.createBook(bookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        BookDTO updatedBook = bookService.updateBook(id, bookDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
