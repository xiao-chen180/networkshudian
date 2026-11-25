package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @EntityGraph(attributePaths = "category")
    List<Book> findAll();

    @EntityGraph(attributePaths = "category")
    Page<Book> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    List<Book> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = "category")
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    List<Book> findByNameContainingOrAuthorContainingOrIsbnContaining(String name, String author, String isbn);

    @EntityGraph(attributePaths = "category")
    Page<Book> findByNameContainingOrAuthorContainingOrIsbnContaining(
            String name, String author, String isbn, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Book> findByReviewCountGreaterThan(Integer reviewCount, Pageable pageable);

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}