package com.example.demo.repository;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);

    // --- Cafe-scoped ---
    List<Category> findByCafe(Cafe cafe);
    boolean existsByNameAndCafe(String name, Cafe cafe);
}
