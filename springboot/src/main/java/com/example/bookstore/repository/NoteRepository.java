package com.example.bookstore.repository;

import com.example.bookstore.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByBookIdAndIsPublicOrderByCreatedAtDesc(Long bookId, Boolean isPublic);
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);
}
