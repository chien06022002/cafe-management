package com.example.demo.service;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Category;
import com.example.demo.entity.MenuItem;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    // --- Category ---
    public List<Category> findAllCategories(Cafe cafe) {
        if (cafe == null) return categoryRepository.findAll();
        return categoryRepository.findByCafe(cafe);
    }

    public Optional<Category> findCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    public boolean categoryExists(String name, Cafe cafe) {
        if (cafe == null) return categoryRepository.existsByName(name);
        return categoryRepository.existsByNameAndCafe(name, cafe);
    }

    // --- MenuItem ---
    public List<MenuItem> findAllItems(Cafe cafe) {
        if (cafe == null) return menuItemRepository.findAll();
        return menuItemRepository.findByCafe(cafe);
    }

    public List<MenuItem> findAvailableItems(Cafe cafe) {
        if (cafe == null) return menuItemRepository.findByAvailable(true);
        return menuItemRepository.findByCafeAndAvailable(cafe);
    }

    public List<MenuItem> findItemsByCategory(Long categoryId) {
        Category cat = categoryRepository.findById(categoryId).orElseThrow();
        return menuItemRepository.findByCategory(cat);
    }

    public Optional<MenuItem> findItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    public MenuItem saveItem(MenuItem item) {
        return menuItemRepository.save(item);
    }

    public void deleteItemById(Long id) {
        menuItemRepository.deleteById(id);
    }

    public List<MenuItem> searchItems(String keyword, Cafe cafe) {
        if (keyword == null || keyword.isBlank()) return findAllItems(cafe);
        if (cafe == null) return menuItemRepository.findByNameContainingIgnoreCase(keyword);
        return menuItemRepository.findByCafeAndNameContaining(cafe, keyword);
    }

    public long countItems(Cafe cafe) {
        if (cafe == null) return menuItemRepository.count();
        return menuItemRepository.countByCafe(cafe);
    }

    public long countAvailableItems(Cafe cafe) {
        if (cafe == null) return menuItemRepository.findByAvailable(true).size();
        return menuItemRepository.countByCafeAndAvailable(cafe);
    }
}
