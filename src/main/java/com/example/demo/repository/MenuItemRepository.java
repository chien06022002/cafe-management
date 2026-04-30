package com.example.demo.repository;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Category;
import com.example.demo.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategory(Category category);
    List<MenuItem> findByAvailable(boolean available);
    List<MenuItem> findByNameContainingIgnoreCase(String name);
    List<MenuItem> findByCategoryAndAvailable(Category category, boolean available);

    // --- Cafe-scoped ---
    @Query("SELECT m FROM MenuItem m WHERE m.category.cafe = :cafe")
    List<MenuItem> findByCafe(@Param("cafe") Cafe cafe);

    @Query("SELECT m FROM MenuItem m WHERE m.category.cafe = :cafe AND m.available = true")
    List<MenuItem> findByCafeAndAvailable(@Param("cafe") Cafe cafe);

    @Query("SELECT m FROM MenuItem m WHERE m.category.cafe = :cafe AND LOWER(m.name) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    List<MenuItem> findByCafeAndNameContaining(@Param("cafe") Cafe cafe, @Param("keyword") String keyword);

    @Query("SELECT COUNT(m) FROM MenuItem m WHERE m.category.cafe = :cafe")
    long countByCafe(@Param("cafe") Cafe cafe);

    @Query("SELECT COUNT(m) FROM MenuItem m WHERE m.category.cafe = :cafe AND m.available = true")
    long countByCafeAndAvailable(@Param("cafe") Cafe cafe);

    @Query("SELECT m FROM MenuItem m WHERE m.category.cafe = :cafe AND m.bestSeller = true")
    List<MenuItem> findBestSellersByCafe(@Param("cafe") Cafe cafe);
}
