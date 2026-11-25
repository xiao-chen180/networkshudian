package com.example.bookstore.controller;

import com.example.bookstore.dto.NoteDTO;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final CurrentUser currentUser;

    public NoteController(NoteService noteService, CurrentUser currentUser) {
        this.noteService = noteService;
        this.currentUser = currentUser;
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<NoteDTO>> getByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(noteService.getNotesByBookId(bookId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<NoteDTO>> getMyNotes(HttpServletRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(noteService.getMyNotes(userId));
    }

    @PostMapping
    public ResponseEntity<NoteDTO> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long bookId = Long.valueOf(body.get("bookId").toString());
        String title = body.get("title") != null ? body.get("title").toString() : "";
        String content = body.get("content") != null ? body.get("content").toString() : "";
        Boolean isPublic = body.get("isPublic") != null ? (Boolean) body.get("isPublic") : true;
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(userId, bookId, title, content, isPublic));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDTO> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(noteService.updateNote(userId, id, body.get("title"), body.get("content")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        noteService.deleteNote(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id) {
        noteService.likeNote(id);
        return ResponseEntity.ok().build();
    }
}
