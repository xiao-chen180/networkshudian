package com.example.bookstore.service;

import com.example.bookstore.dto.TagDTO;
import com.example.bookstore.entity.BookTag;
import com.example.bookstore.entity.Tag;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.BookTagRepository;
import com.example.bookstore.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final BookTagRepository bookTagRepository;

    public TagService(TagRepository tagRepository, BookTagRepository bookTagRepository) {
        this.tagRepository = tagRepository;
        this.bookTagRepository = bookTagRepository;
    }

    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TagDTO> getTagsByBookId(Long bookId) {
        List<BookTag> bookTags = bookTagRepository.findByBookId(bookId);
        return bookTags.stream()
                .map(bt -> tagRepository.findById(bt.getTagId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TagDTO createTag(String name) {
        if (tagRepository.existsByName(name)) {
            throw new CustomException("标签已存在", HttpStatus.BAD_REQUEST.value());
        }
        Tag tag = new Tag();
        tag.setName(name);
        Tag savedTag = tagRepository.save(tag);
        return convertToDTO(savedTag);
    }

    public void addTagToBook(Long bookId, Long tagId) {
        if (bookTagRepository.existsByBookIdAndTagId(bookId, tagId)) {
            throw new CustomException("该书已有此标签", HttpStatus.BAD_REQUEST.value());
        }
        BookTag bookTag = new BookTag();
        bookTag.setBookId(bookId);
        bookTag.setTagId(tagId);
        bookTagRepository.save(bookTag);
    }

    public void removeTagFromBook(Long bookId, Long tagId) {
        bookTagRepository.deleteByBookIdAndTagId(bookId, tagId);
    }

    public void deleteTag(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new CustomException("标签不存在", HttpStatus.NOT_FOUND.value());
        }
        tagRepository.deleteById(tagId);
    }

    private TagDTO convertToDTO(Tag tag) {
        return new TagDTO(tag.getId(), tag.getName());
    }
}
