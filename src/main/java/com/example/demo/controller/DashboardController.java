package com.example.demo.controller;

import com.example.demo.entity.Cafe;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final MenuService menuService;
    private final CurrentUserService currentUserService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        model.addAttribute("totalEmployees", employeeService.findAll(cafe).size());
        model.addAttribute("activeEmployees", employeeService.findActive(cafe).size());
        model.addAttribute("totalMenuItems", menuService.countItems(cafe));
        model.addAttribute("availableItems", menuService.countAvailableItems(cafe));
        model.addAttribute("presentToday", attendanceService.countPresentToday(cafe));
        model.addAttribute("absentToday", attendanceService.countAbsentToday(cafe));
        model.addAttribute("todayAttendance", attendanceService.findByDate(LocalDate.now(), cafe));
        model.addAttribute("today", LocalDate.now());
        return "dashboard/index";
    }
}
