package com.example.bookstore.service;

import com.example.bookstore.dto.NoteDTO;
import com.example.bookstore.entity.Note;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.NoteRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<NoteDTO> getNotesByBookId(Long bookId) {
        return noteRepository.findByBookIdAndIsPublicOrderByCreatedAtDesc(bookId, true).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getMyNotes(Long userId) {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public NoteDTO createNote(Long userId, Long bookId, String title, String content, Boolean isPublic) {
        Note note = new Note();
        note.setUserId(userId);
        note.setBookId(bookId);
        note.setTitle(title);
        note.setContent(content);
        note.setIsPublic(isPublic != null ? isPublic : true);
        Note saved = noteRepository.save(note);
        return convertToDTO(saved);
    }

    public NoteDTO updateNote(Long userId, Long noteId, String title, String content) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new CustomException("笔记不存在", HttpStatus.NOT_FOUND.value()));
        if (!note.getUserId().equals(userId)) throw new CustomException("无权修改", HttpStatus.FORBIDDEN.value());
        if (title != null) note.setTitle(title);
        if (content != null) note.setContent(content);
        return convertToDTO(noteRepository.save(note));
    }

    public void deleteNote(Long userId, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new CustomException("笔记不存在", HttpStatus.NOT_FOUND.value()));
        if (!note.getUserId().equals(userId)) throw new CustomException("无权删除", HttpStatus.FORBIDDEN.value());
        noteRepository.deleteById(noteId);
    }

    public void likeNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new CustomException("笔记不存在", HttpStatus.NOT_FOUND.value()));
        note.setLikeCount(note.getLikeCount() + 1);
        noteRepository.save(note);
    }

    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setUserId(note.getUserId());
        dto.setBookId(note.getBookId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setIsPublic(note.getIsPublic());
        dto.setLikeCount(note.getLikeCount());
        dto.setCreatedAt(note.getCreatedAt());
        userRepository.findById(note.getUserId()).ifPresent(u -> dto.setUsername(u.getUsername()));
        bookRepository.findById(note.getBookId()).ifPresent(b -> dto.setBookName(b.getName()));
        return dto;
    }
}
