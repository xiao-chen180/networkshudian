package com.example.bookstore.controller;

import com.example.bookstore.dto.TagDTO;
import com.example.bookstore.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<TagDTO>> getTagsByBookId(@PathVariable Long bookId) {
        List<TagDTO> tags = tagService.getTagsByBookId(bookId);
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        TagDTO tag = tagService.createTag(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @PostMapping("/book/{bookId}")
    public ResponseEntity<Void> addTagToBook(
            @PathVariable Long bookId,
            @RequestBody Map<String, Long> body) {
        Long tagId = body.get("tagId");
        tagService.addTagToBook(bookId, tagId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/book/{bookId}/{tagId}")
    public ResponseEntity<Void> removeTagFromBook(
            @PathVariable Long bookId,
            @PathVariable Long tagId) {
        tagService.removeTagFromBook(bookId, tagId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
