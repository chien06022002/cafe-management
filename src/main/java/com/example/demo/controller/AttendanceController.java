package com.example.demo.controller;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Long employeeId,
            Model model) {

        Cafe cafe = currentUserService.getCurrentCafe();
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();
        model.addAttribute("attendances",
                employeeId != null
                        ? attendanceService.findByEmployee(employeeId)
                        : attendanceService.findByDate(selectedDate, cafe));
        model.addAttribute("employees", employeeService.findAll(cafe));
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedEmployeeId", employeeId);
        return "attendance/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        Attendance attendance = new Attendance();
        attendance.setDate(LocalDate.now());
        model.addAttribute("attendance", attendance);
        model.addAttribute("employees", employeeService.findActive(cafe));
        model.addAttribute("isEdit", false);
        return "attendance/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Attendance attendance,
                         @RequestParam Long employeeId,
                         RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
        if (attendanceService.existsByEmployeeAndDate(employee, attendance.getDate())) {
            redirectAttributes.addFlashAttribute("error", "Nhân viên đã có dữ liệu chấm công ngày này!");
            return "redirect:/attendance/new";
        }
        attendance.setEmployee(employee);
        attendanceService.save(attendance);
        redirectAttributes.addFlashAttribute("success", "Chấm công thành công!");
        return "redirect:/attendance";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        Attendance attendance = attendanceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi chấm công"));
        model.addAttribute("attendance", attendance);
        model.addAttribute("employees", employeeService.findActive(cafe));
        model.addAttribute("isEdit", true);
        return "attendance/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute Attendance attendance,
                         @RequestParam Long employeeId,
                         RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
        attendance.setId(id);
        attendance.setEmployee(employee);
        attendanceService.save(attendance);
        redirectAttributes.addFlashAttribute("success", "Cập nhật chấm công thành công!");
        return "redirect:/attendance";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        attendanceService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Xóa bản ghi chấm công thành công!");
        return "redirect:/attendance";
    }
}
