package com.example.demo.dto.shift;

import com.example.demo.dto.common.EmployeeSummaryDto;

import java.util.List;

public record WeeklyScheduleDto(
        List<ShiftAssignmentDto> assignments,
        List<ShiftDto> weekShifts,
        List<EmployeeSummaryDto> employees,
        String weekStart,
        String weekEnd,
        List<String> weekDays
) {
}
