package com.example.demo.dto.employee;

import java.math.BigDecimal;

public record EmployeeDto(
        Long id,
        String employeeCode,
        String fullName,
        String phone,
        String email,
        String address,
        String position,
        String department,
        String hireDate,
        BigDecimal salary,
        String status,
        String avatarUrl,
        boolean hasFaceData,
        Long userId
) {
}

