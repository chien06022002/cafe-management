package com.example.demo.dto.cafe;

public record CafeDto(
        Long id,
        String name,
        String address,
        String phone,
        String description,
        boolean active
) {
}

