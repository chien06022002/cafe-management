package com.example.demo.controller;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Category;
import com.example.demo.entity.MenuItem;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final CurrentUserService currentUserService;

    // ---- Categories ----
    @GetMapping("/categories")
    public String categories(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        model.addAttribute("categories", menuService.findAllCategories(cafe));
        model.addAttribute("formCategory", new Category());
        model.addAttribute("isEditCategory", false);
        return "menu/categories";
    }

    @PostMapping("/categories/new")
    public String createCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        Cafe cafe = currentUserService.getCurrentCafe();
        if (menuService.categoryExists(category.getName(), cafe)) {
            redirectAttributes.addFlashAttribute("error", "Danh mục đã tồn tại!");
            return "redirect:/menu/categories";
        }
        category.setCafe(cafe);
        menuService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công!");
        return "redirect:/menu/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        menuService.deleteCategoryById(id);
        redirectAttributes.addFlashAttribute("success", "Xóa danh mục thành công!");
        return "redirect:/menu/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String showEditCategory(@PathVariable Long id, Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        Category category = menuService.findCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
        model.addAttribute("categories", menuService.findAllCategories(cafe));
        model.addAttribute("formCategory", category);
        model.addAttribute("isEditCategory", true);
        return "menu/categories";
    }

    @PostMapping("/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category,
                                  RedirectAttributes redirectAttributes) {
        Category existing = menuService.findCategoryById(id).orElseThrow();
        category.setId(id);
        category.setCafe(existing.getCafe());
        menuService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục thành công!");
        return "redirect:/menu/categories";
    }

    // ---- Menu Items ----
    @GetMapping("/items")
    public String items(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long categoryId,
                        Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        model.addAttribute("items", categoryId != null
                ? menuService.findItemsByCategory(categoryId)
                : menuService.searchItems(keyword, cafe));
        model.addAttribute("categories", menuService.findAllCategories(cafe));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        return "menu/items";
    }

    @GetMapping("/items/new")
    public String showCreateItem(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        model.addAttribute("item", new MenuItem());
        model.addAttribute("categories", menuService.findAllCategories(cafe));
        model.addAttribute("isEdit", false);
        return "menu/item-form";
    }

    @PostMapping("/items/new")
    public String createItem(@ModelAttribute MenuItem item,
                              @RequestParam(required = false) Long categoryId,
                              RedirectAttributes redirectAttributes) {
        if (categoryId != null) {
            menuService.findCategoryById(categoryId).ifPresent(item::setCategory);
        }
        menuService.saveItem(item);
        redirectAttributes.addFlashAttribute("success", "Thêm món ăn thành công!");
        return "redirect:/menu/items";
    }

    @GetMapping("/items/{id}/edit")
    public String showEditItem(@PathVariable Long id, Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        MenuItem item = menuService.findItemById(id)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại"));
        model.addAttribute("item", item);
        model.addAttribute("categories", menuService.findAllCategories(cafe));
        model.addAttribute("isEdit", true);
        return "menu/item-form";
    }

    @PostMapping("/items/{id}/edit")
    public String updateItem(@PathVariable Long id,
                              @ModelAttribute MenuItem item,
                              @RequestParam(required = false) Long categoryId,
                              RedirectAttributes redirectAttributes) {
        item.setId(id);
        if (categoryId != null) {
            menuService.findCategoryById(categoryId).ifPresent(item::setCategory);
        }
        menuService.saveItem(item);
        redirectAttributes.addFlashAttribute("success", "Cập nhật món ăn thành công!");
        return "redirect:/menu/items";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        menuService.deleteItemById(id);
        redirectAttributes.addFlashAttribute("success", "Xóa món ăn thành công!");
        return "redirect:/menu/items";
    }
}
