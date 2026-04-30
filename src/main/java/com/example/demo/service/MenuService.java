package com.example.demo.service;

import com.example.demo.dto.menu.CategoryDto;
import com.example.demo.dto.menu.MenuItemDto;
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

    public List<MenuItem> findBestSellers(Cafe cafe) {
        if (cafe == null) return menuItemRepository.findAll().stream().filter(MenuItem::isBestSeller).toList();
        return menuItemRepository.findBestSellersByCafe(cafe);
    }

    public long countItems(Cafe cafe) {
        if (cafe == null) return menuItemRepository.count();
        return menuItemRepository.countByCafe(cafe);
    }

    public long countAvailableItems(Cafe cafe) {
        if (cafe == null) return menuItemRepository.findByAvailable(true).size();
        return menuItemRepository.countByCafeAndAvailable(cafe);
    }

    // ── DTO mappers ──

    public CategoryDto toCategoryDto(Category cat) {
        if (cat == null) return null;
        return new CategoryDto(cat.getId(), cat.getName(), cat.getDescription());
    }

    public java.util.List<CategoryDto> toCategoryDtoList(java.util.List<Category> list) {
        return list.stream().map(this::toCategoryDto).toList();
    }

    public MenuItemDto toMenuItemDto(MenuItem item) {
        if (item == null) return null;
        Long catId = item.getCategory() != null ? item.getCategory().getId() : null;
        String catName = item.getCategory() != null ? item.getCategory().getName() : null;
        return new MenuItemDto(
                item.getId(), item.getName(), item.getDescription(),
                item.getPrice(), item.getImageUrl(), item.isAvailable(),
                item.isBestSeller(),
                catId, catName
        );
    }

    public java.util.List<MenuItemDto> toMenuItemDtoList(java.util.List<MenuItem> list) {
        return list.stream().map(this::toMenuItemDto).toList();
    }
}
