package com.example.demo.controller.api;

import com.example.demo.entity.MenuItem;
import com.example.demo.entity.Sop;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.MenuService;
import com.example.demo.service.SopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sop")
@RequiredArgsConstructor
public class SopApiController {

    private final SopService sopService;
    private final MenuService menuService;
    private final CurrentUserService currentUserService;

    @GetMapping("/items")
    public ResponseEntity<?> listItems() {
        var cafe = currentUserService.getCurrentCafe();
        return ResponseEntity.ok(menuService.toMenuItemDtoList(menuService.findAllItems(cafe)));
    }

    @GetMapping("/item/{menuItemId}")
    public ResponseEntity<?> getSops(@PathVariable Long menuItemId) {
        MenuItem item = menuService.findItemById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại"));
        return ResponseEntity.ok(Map.of(
                "menuItem", menuService.toMenuItemDto(item),
                "sops", sopService.toDtoList(sopService.findByMenuItem(menuItemId))));
    }

    @PostMapping("/item/{menuItemId}")
    public ResponseEntity<?> create(@PathVariable Long menuItemId, @RequestBody Sop sop) {
        MenuItem item = menuService.findItemById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại"));
        if (sop.getStepOrder() == null) sop.setStepOrder(sopService.getNextStepOrder(menuItemId));
        sop.setMenuItem(item);
        return ResponseEntity.ok(sopService.toDto(sopService.save(sop)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Sop sop) {
        return sopService.findById(id).map(existing -> {
            sop.setId(id);
            sop.setMenuItem(existing.getMenuItem());
            return ResponseEntity.ok(sopService.toDto(sopService.save(sop)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        sopService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }
}
