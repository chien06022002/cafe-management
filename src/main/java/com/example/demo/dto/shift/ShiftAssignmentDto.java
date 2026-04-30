package com.example.demo.dto.shift;

import com.example.demo.dto.common.EmployeeSummaryDto;

import java.time.LocalDate;

public record ShiftAssignmentDto(
        Long id,
        LocalDate date,
        String note,
        ShiftDto shift,
        EmployeeSummaryDto employee
) {
}
