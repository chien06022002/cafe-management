package com.example.demo.service;

import com.example.demo.entity.MenuItem;
import com.example.demo.entity.Sop;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.repository.SopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SopService {

    private final SopRepository sopRepository;
    private final MenuItemRepository menuItemRepository;

    public List<Sop> findAll() {
        return sopRepository.findAll();
    }

    public List<Sop> findByMenuItem(Long menuItemId) {
        MenuItem item = menuItemRepository.findById(menuItemId).orElseThrow();
        return sopRepository.findByMenuItemOrderByStepOrderAsc(item);
    }

    public Optional<Sop> findById(Long id) {
        return sopRepository.findById(id);
    }

    public Sop save(Sop sop) {
        return sopRepository.save(sop);
    }

    public void deleteById(Long id) {
        sopRepository.deleteById(id);
    }

    public int getNextStepOrder(Long menuItemId) {
        List<Sop> sops = sopRepository.findByMenuItemId(menuItemId);
        return sops.stream().mapToInt(Sop::getStepOrder).max().orElse(0) + 1;
    }
}
