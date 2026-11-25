package com.example.bookstore.repository;

import com.example.bookstore.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookTagRepository extends JpaRepository<BookTag, Long> {
    List<BookTag> findByBookId(Long bookId);
    List<BookTag> findByTagId(Long tagId);
    void deleteByBookId(Long bookId);
    void deleteByBookIdAndTagId(Long bookId, Long tagId);
    boolean existsByBookIdAndTagId(Long bookId, Long tagId);
}
