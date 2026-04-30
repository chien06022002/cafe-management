package com.example.demo.dto.common;

public record EmployeeSummaryDto(
        Long id,
        String employeeCode,
        String fullName,
        String position,
        String avatarUrl
) {
}

