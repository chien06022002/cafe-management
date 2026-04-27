package com.example.demo.controller;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    // ===== QUẢN LÝ CA =====
    @GetMapping("/manage")
    public String manageShifts(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        model.addAttribute("shifts", shiftService.findAllShifts(cafe));
        model.addAttribute("formShift", new Shift());
        model.addAttribute("isEditShift", false);
        return "shift/manage";
    }

    @PostMapping("/manage/new")
    public String createShift(@ModelAttribute Shift shift, RedirectAttributes ra) {
        Cafe cafe = currentUserService.getCurrentCafe();
        if (shiftService.shiftNameExists(shift.getName(), cafe)) {
            ra.addFlashAttribute("error", "Tên ca đã tồn tại!");
            return "redirect:/shifts/manage";
        }
        shift.setCafe(cafe);
        shiftService.saveShift(shift);
        ra.addFlashAttribute("success", "Thêm ca làm việc thành công!");
        return "redirect:/shifts/manage";
    }

    @GetMapping("/manage/{id}/edit")
    public String showEditShift(@PathVariable Long id, Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        Shift shift = shiftService.findShiftById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ca không tồn tại"));
        model.addAttribute("shifts", shiftService.findAllShifts(cafe));
        model.addAttribute("formShift", shift);
        model.addAttribute("isEditShift", true);
        return "shift/manage";
    }

    @PostMapping("/manage/{id}/edit")
    public String updateShift(@PathVariable Long id, @ModelAttribute Shift shift, RedirectAttributes ra) {
        Shift existing = shiftService.findShiftById(id).orElseThrow();
        shift.setId(id);
        shift.setCafe(existing.getCafe());
        shiftService.saveShift(shift);
        ra.addFlashAttribute("success", "Cập nhật ca thành công!");
        return "redirect:/shifts/manage";
    }

    @PostMapping("/manage/{id}/delete")
    public String deleteShift(@PathVariable Long id, RedirectAttributes ra) {
        shiftService.deleteShiftById(id);
        ra.addFlashAttribute("success", "Xóa ca thành công!");
        return "redirect:/shifts/manage";
    }

    // ===== PHÂN CA =====
    @GetMapping
    public String schedule(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStart,
            Model model) {

        Cafe cafe = currentUserService.getCurrentCafe();
        if (weekStart == null) {
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        LocalDate weekEnd = weekStart.plusDays(6);

        List<ShiftAssignment> assignments = shiftService.findWeeklySchedule(weekStart, cafe);

        // Distinct shifts sorted by startTime (from assignments first, fallback to all active shifts)
        List<Shift> weekShifts = assignments.stream()
                .collect(Collectors.toMap(
                        a -> a.getShift().getId(),
                        ShiftAssignment::getShift,
                        (a, b) -> a,
                        LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(Shift::getStartTime))
                .collect(Collectors.toList());

        if (weekShifts.isEmpty()) {
            weekShifts = shiftService.findActiveShifts(cafe);
        }

        model.addAttribute("assignments", assignments);
        model.addAttribute("weekShifts", weekShifts);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("prevWeek", weekStart.minusWeeks(1));
        model.addAttribute("nextWeek", weekStart.plusWeeks(1));
        model.addAttribute("today", LocalDate.now());

        LocalDate[] weekDays = new LocalDate[7];
        for (int i = 0; i < 7; i++) weekDays[i] = weekStart.plusDays(i);
        model.addAttribute("weekDays", weekDays);
        return "shift/schedule";
    }

    @GetMapping("/assign")
    public String showAssignForm(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        model.addAttribute("assignment", new ShiftAssignment());
        model.addAttribute("employees", employeeService.findActive(cafe));
        model.addAttribute("shifts", shiftService.findActiveShifts(cafe));
        model.addAttribute("isEdit", false);
        return "shift/assign-form";
    }

    @PostMapping("/assign")
    public String assign(@ModelAttribute ShiftAssignment assignment,
                          @RequestParam Long employeeId,
                          @RequestParam Long shiftId,
                          RedirectAttributes ra) {
        Employee emp = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
        Shift shift = shiftService.findShiftById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Ca không tồn tại"));

        if (shiftService.assignmentExists(emp, assignment.getDate())) {
            ra.addFlashAttribute("error", "Nhân viên đã được phân ca ngày này!");
            return "redirect:/shifts/assign";
        }
        assignment.setEmployee(emp);
        assignment.setShift(shift);
        shiftService.saveAssignment(assignment);
        ra.addFlashAttribute("success", "Phân ca thành công!");
        return "redirect:/shifts";
    }

    @GetMapping("/assign/{id}/edit")
    public String showEditAssignment(@PathVariable Long id, Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        ShiftAssignment assignment = shiftService.findAssignmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phân ca"));
        model.addAttribute("assignment", assignment);
        model.addAttribute("employees", employeeService.findActive(cafe));
        model.addAttribute("shifts", shiftService.findActiveShifts(cafe));
        model.addAttribute("isEdit", true);
        return "shift/assign-form";
    }

    @PostMapping("/assign/{id}/edit")
    public String updateAssignment(@PathVariable Long id,
                                    @ModelAttribute ShiftAssignment assignment,
                                    @RequestParam Long employeeId,
                                    @RequestParam Long shiftId,
                                    RedirectAttributes ra) {
        Employee emp = employeeService.findById(employeeId).orElseThrow();
        Shift shift = shiftService.findShiftById(shiftId).orElseThrow();
        assignment.setId(id);
        assignment.setEmployee(emp);
        assignment.setShift(shift);
        shiftService.saveAssignment(assignment);
        ra.addFlashAttribute("success", "Cập nhật phân ca thành công!");
        return "redirect:/shifts";
    }

    @PostMapping("/assign/{id}/delete")
    public String deleteAssignment(@PathVariable Long id, RedirectAttributes ra) {
        shiftService.deleteAssignmentById(id);
        ra.addFlashAttribute("success", "Xóa phân ca thành công!");
        return "redirect:/shifts";
    }
}
