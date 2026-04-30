package com.example.demo.dto.menu;

import java.math.BigDecimal;

public record MenuItemDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean available,
        boolean bestSeller,
        Long categoryId,
        String categoryName
) {
}

