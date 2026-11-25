package com.example.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"book_id", "tag_id"})
})
public class BookTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    public BookTag() {}

    public BookTag(Long id, Long bookId, Long tagId) {
        this.id = id;
        this.bookId = bookId;
        this.tagId = tagId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
}
