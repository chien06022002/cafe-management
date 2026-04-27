package com.example.demo.repository;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByActive(boolean active);
    boolean existsByName(String name);

    // --- Cafe-scoped ---
    List<Shift> findByCafe(Cafe cafe);
    List<Shift> findByCafeAndActive(Cafe cafe, boolean active);
    boolean existsByNameAndCafe(String name, Cafe cafe);
}
