package com.onlinebookstore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.Category;
import com.onlinebookstore.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> all() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category save(Category category) {
        category.setName(category.getName().trim());
        if (category.getId() == null && categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("A category named \"" + category.getName() + "\" already exists.");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
