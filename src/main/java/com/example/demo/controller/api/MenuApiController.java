package com.example.demo.controller.api;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Category;
import com.example.demo.entity.MenuItem;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuApiController {

    private final MenuService menuService;
    private final CurrentUserService currentUserService;

    // ── Categories ──
    @GetMapping("/categories")
    public ResponseEntity<?> listCategories() {
        Cafe cafe = currentUserService.getCurrentCafe();
        return ResponseEntity.ok(menuService.toCategoryDtoList(menuService.findAllCategories(cafe)));
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        Cafe cafe = currentUserService.getCurrentCafe();
        if (menuService.categoryExists(category.getName(), cafe)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Danh mục đã tồn tại"));
        }
        category.setCafe(cafe);
        return ResponseEntity.ok(menuService.toCategoryDto(menuService.saveCategory(category)));
    }

    @PutMapping("/categories/{id:\\d+}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return menuService.findCategoryById(id).map(existing -> {
            category.setId(id);
            category.setCafe(existing.getCafe());
            return ResponseEntity.ok(menuService.toCategoryDto(menuService.saveCategory(category)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/categories/{id:\\d+}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategoryById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }

    // ── Items ──
    @GetMapping("/items/best-sellers")
    public ResponseEntity<?> listBestSellers() {
        Cafe cafe = currentUserService.getCurrentCafe();
        return ResponseEntity.ok(menuService.toMenuItemDtoList(menuService.findBestSellers(cafe)));
    }

    @GetMapping("/items")
    public ResponseEntity<?> listItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        Cafe cafe = currentUserService.getCurrentCafe();
        var items = categoryId != null
                ? menuService.findItemsByCategory(categoryId)
                : menuService.searchItems(keyword, cafe);
        return ResponseEntity.ok(menuService.toMenuItemDtoList(items));
    }

    @GetMapping("/items/{id:\\d+}")
    public ResponseEntity<?> getItem(@PathVariable Long id) {
        return menuService.findItemById(id)
                .map(menuService::toMenuItemDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    public ResponseEntity<?> createItem(@RequestBody Map<String, Object> body) {
        MenuItem item = buildMenuItem(body);
        parseLongOrNull(body.get("categoryId"))
                .flatMap(menuService::findCategoryById)
                .ifPresent(item::setCategory);
        return ResponseEntity.ok(menuService.toMenuItemDto(menuService.saveItem(item)));
    }

    @PutMapping("/items/{id:\\d+}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return menuService.findItemById(id).map(existing -> {
            MenuItem item = buildMenuItem(body);
            item.setId(id);
            var categoryId = parseLongOrNull(body.get("categoryId"));
            if (categoryId.isPresent()) {
                menuService.findCategoryById(categoryId.get()).ifPresent(item::setCategory);
            } else if (!body.containsKey("categoryId")) {
                item.setCategory(existing.getCategory());
            }
            return ResponseEntity.ok(menuService.toMenuItemDto(menuService.saveItem(item)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/items/{id:\\d+}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        menuService.deleteItemById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }

    private MenuItem buildMenuItem(Map<String, Object> body) {
        MenuItem item = new MenuItem();
        if (body.get("name") != null) item.setName(body.get("name").toString());
        if (body.get("description") != null) item.setDescription(body.get("description").toString());
        if (body.get("price") != null) item.setPrice(new BigDecimal(body.get("price").toString()));
        if (body.get("imageUrl") != null) item.setImageUrl(body.get("imageUrl").toString());
        item.setAvailable(body.get("available") == null || Boolean.parseBoolean(body.get("available").toString()));
        item.setBestSeller(body.get("bestSeller") != null && Boolean.parseBoolean(body.get("bestSeller").toString()));
        return item;
    }

    private java.util.Optional<Long> parseLongOrNull(Object value) {
        if (value == null) return java.util.Optional.empty();
        String raw = value.toString().trim();
        if (raw.isEmpty()) return java.util.Optional.empty();
        try {
            return java.util.Optional.of(Long.parseLong(raw));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }
    }
}
