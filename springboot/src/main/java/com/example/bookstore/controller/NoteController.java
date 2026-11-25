package com.example.bookstore.controller;

import com.example.bookstore.dto.CreateNoteRequest;
import com.example.bookstore.dto.NoteDTO;
import com.example.bookstore.dto.UpdateNoteRequest;
import com.example.bookstore.security.CurrentUser;
import com.example.bookstore.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<NoteDTO>> getMyNotes() {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(noteService.getMyNotes(userId));
    }

    @PostMapping
    public ResponseEntity<NoteDTO> create(@Valid @RequestBody CreateNoteRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        NoteDTO note = noteService.createNote(
                userId,
                request.bookId(),
                request.title(),
                request.content() != null ? request.content() : "",
                request.isPublic() == null || request.isPublic());
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDTO> update(@PathVariable Long id,
                                          @Valid @RequestBody UpdateNoteRequest request) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(noteService.updateNote(userId, id, request.title(), request.content()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        noteService.deleteNote(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id) {
        noteService.likeNote(id);
        return ResponseEntity.ok().build();
    }
}