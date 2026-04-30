package com.example.demo.dto.attendance;

import com.example.demo.dto.common.EmployeeSummaryDto;

public record AttendanceDto(
        Long id,
        EmployeeSummaryDto employee,
        String date,
        String checkIn,
        String checkOut,
        String status,
        String note
) {
}

