package com.example.demo.dto.payroll;

import com.example.demo.dto.common.EmployeeSummaryDto;

import java.math.BigDecimal;

public record PayrollDto(
        Long id,
        EmployeeSummaryDto employee,
        Integer month,
        Integer year,
        BigDecimal baseSalary,
        Integer standardWorkDays,
        Integer totalWorkDays,   // alias of standardWorkDays for frontend compatibility
        Integer actualWorkDays,
        Integer paidLeaveDays,
        BigDecimal overtimeHours,
        BigDecimal overtimeRate,
        BigDecimal allowance,
        BigDecimal deduction,
        BigDecimal deductions,   // alias of deduction for frontend compatibility
        BigDecimal netSalary,
        String status,
        String note,
        String paidDate
) {
}

