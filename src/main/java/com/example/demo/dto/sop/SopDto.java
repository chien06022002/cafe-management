package com.example.demo.dto.sop;

public record SopDto(
        Long id,
        Long menuItemId,
        String menuItemName,
        String title,
        String content,
        Integer stepOrder,
        String createdDate,
        String updatedDate
) {
}

