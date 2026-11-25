
package com.example.bookstore.service;

import com.example.bookstore.entity.Category;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("分类不存在", HttpStatus.NOT_FOUND.value()));
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new CustomException("分类名称已存在", HttpStatus.BAD_REQUEST.value());
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("分类不存在", HttpStatus.NOT_FOUND.value()));

        if (!existingCategory.getName().equals(category.getName()) 
                && categoryRepository.existsByName(category.getName())) {
            throw new CustomException("分类名称已存在", HttpStatus.BAD_REQUEST.value());
        }

        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CustomException("分类不存在", HttpStatus.NOT_FOUND.value());
        }
        categoryRepository.deleteById(id);
    }
}
