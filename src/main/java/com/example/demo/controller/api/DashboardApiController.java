package com.example.demo.controller.api;

import com.example.demo.entity.Cafe;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final MenuService menuService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<?> stats() {
        Cafe cafe = currentUserService.getCurrentCafe();
        return ResponseEntity.ok(Map.of(
                "totalEmployees", employeeService.findAll(cafe).size(),
                "activeEmployees", employeeService.findActive(cafe).size(),
                "totalMenuItems", menuService.countItems(cafe),
                "availableItems", menuService.countAvailableItems(cafe),
                "presentToday", attendanceService.countPresentToday(cafe),
                "absentToday", attendanceService.countAbsentToday(cafe),
                "todayAttendance", attendanceService.toDtoList(
                        attendanceService.findByDate(LocalDate.now(), cafe))));
    }
}
