package com.example.demo.controller.api;

import com.example.demo.dto.cafe.CafeDto;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CafeService;
import com.example.demo.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cafe")
@RequiredArgsConstructor
public class CafeApiController {

    private final CafeService cafeService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        Cafe cafe = currentUserService.getCurrentCafe();
        if (cafe == null) cafe = new Cafe();
        return ResponseEntity.ok(toDto(cafe));
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Cafe incoming) {
        User user = currentUserService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();

        Cafe saved;
        if (incoming.getId() != null) {
            Cafe existing = cafeService.findById(incoming.getId()).orElseThrow();
            existing.setName(incoming.getName());
            existing.setAddress(incoming.getAddress());
            existing.setPhone(incoming.getPhone());
            existing.setDescription(incoming.getDescription());
            saved = cafeService.save(existing);
        } else {
            saved = cafeService.save(incoming);
        }

        if (user.getCafe() == null) {
            user.setCafe(saved);
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of("message", "Cập nhật thành công", "cafe", toDto(saved)));
    }

    private CafeDto toDto(Cafe c) {
        return new CafeDto(c.getId(), c.getName(), c.getAddress(), c.getPhone(), c.getDescription(), c.isActive());
    }
}
