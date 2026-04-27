package com.example.demo.repository;

import com.example.demo.entity.MenuItem;
import com.example.demo.entity.Sop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SopRepository extends JpaRepository<Sop, Long> {
    List<Sop> findByMenuItemOrderByStepOrderAsc(MenuItem menuItem);
    List<Sop> findByMenuItemId(Long menuItemId);
}
