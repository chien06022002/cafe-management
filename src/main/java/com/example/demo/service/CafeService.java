package com.example.demo.service;

import com.example.demo.entity.Cafe;
import com.example.demo.repository.CafeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CafeService {

    private final CafeRepository cafeRepository;

    public List<Cafe> findAll() {
        return cafeRepository.findAll();
    }

    public List<Cafe> findActive() {
        return cafeRepository.findByActive(true);
    }

    public Optional<Cafe> findById(Long id) {
        return cafeRepository.findById(id);
    }

    public Cafe save(Cafe cafe) {
        return cafeRepository.save(cafe);
    }

    public void deleteById(Long id) {
        cafeRepository.deleteById(id);
    }
}
