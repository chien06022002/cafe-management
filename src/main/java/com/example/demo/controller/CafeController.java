package com.example.demo.controller;

import com.example.demo.entity.Cafe;
import com.example.demo.service.CafeService;
import com.example.demo.service.CurrentUserService;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cafe")
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @GetMapping("/settings")
    public String settings(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        if (cafe == null) {
            cafe = new Cafe();
        }
        model.addAttribute("cafe", cafe);
        return "cafe/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute Cafe cafe, RedirectAttributes ra) {
        User user = currentUserService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Cafe savedCafe;
        if (cafe.getId() != null) {
            // Update existing
            Cafe existing = cafeService.findById(cafe.getId()).orElseThrow();
            existing.setName(cafe.getName());
            existing.setAddress(cafe.getAddress());
            existing.setPhone(cafe.getPhone());
            existing.setDescription(cafe.getDescription());
            savedCafe = cafeService.save(existing);
        } else {
            // Create new cafe and assign to user
            savedCafe = cafeService.save(cafe);
        }

        // Assign cafe to current user if not already assigned
        if (user.getCafe() == null) {
            user.setCafe(savedCafe);
            userRepository.save(user);
        }

        ra.addFlashAttribute("success", "Cập nhật thông tin quán thành công!");
        return "redirect:/cafe/settings";
    }
}
