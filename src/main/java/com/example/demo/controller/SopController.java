package com.example.demo.controller;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.MenuItem;
import com.example.demo.entity.Sop;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.MenuService;
import com.example.demo.service.SopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sop")
@RequiredArgsConstructor
public class SopController {

    private final SopService sopService;
    private final MenuService menuService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        model.addAttribute("menuItems", menuService.findAllItems(cafe));
        return "sop/list";
    }

    @GetMapping("/item/{menuItemId}")
    public String sopByItem(@PathVariable Long menuItemId, Model model) {
        MenuItem item = menuService.findItemById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại"));
        model.addAttribute("menuItem", item);
        model.addAttribute("sops", sopService.findByMenuItem(menuItemId));
        return "sop/detail";
    }

    @GetMapping("/item/{menuItemId}/new")
    public String showCreateForm(@PathVariable Long menuItemId, Model model) {
        MenuItem item = menuService.findItemById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại"));
        Sop sop = new Sop();
        sop.setStepOrder(sopService.getNextStepOrder(menuItemId));
        model.addAttribute("sop", sop);
        model.addAttribute("menuItem", item);
        model.addAttribute("isEdit", false);
        return "sop/form";
    }

    @PostMapping("/item/{menuItemId}/new")
    public String create(@PathVariable Long menuItemId,
                          @ModelAttribute Sop sop,
                          RedirectAttributes redirectAttributes) {
        MenuItem item = menuService.findItemById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại"));
        sop.setMenuItem(item);
        sopService.save(sop);
        redirectAttributes.addFlashAttribute("success", "Thêm bước SOP thành công!");
        return "redirect:/sop/item/" + menuItemId;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Sop sop = sopService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SOP không tồn tại"));
        model.addAttribute("sop", sop);
        model.addAttribute("menuItem", sop.getMenuItem());
        model.addAttribute("isEdit", true);
        return "sop/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                          @ModelAttribute Sop sop,
                          RedirectAttributes redirectAttributes) {
        Sop existing = sopService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SOP không tồn tại"));
        sop.setId(id);
        sop.setMenuItem(existing.getMenuItem());
        sopService.save(sop);
        redirectAttributes.addFlashAttribute("success", "Cập nhật SOP thành công!");
        return "redirect:/sop/item/" + existing.getMenuItem().getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Sop sop = sopService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SOP không tồn tại"));
        Long menuItemId = sop.getMenuItem().getId();
        sopService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Xóa bước SOP thành công!");
        return "redirect:/sop/item/" + menuItemId;
    }
}
