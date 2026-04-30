package com.example.demo.dto.shift;

import java.time.LocalTime;

public record ShiftDto(
        Long id,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        String description,
        boolean active
) {
}

