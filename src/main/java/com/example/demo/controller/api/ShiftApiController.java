package com.example.demo.controller.api;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ShiftApiController {

    private final ShiftService shiftService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    // ── Shifts ──
    @GetMapping("/api/shifts")
    public ResponseEntity<?> listShifts() {
        Cafe cafe = currentUserService.getCurrentCafe();
        return ResponseEntity.ok(shiftService.findAllShiftDtos(cafe));
    }

    @PostMapping("/api/shifts")
    public ResponseEntity<?> createShift(@RequestBody Map<String, Object> body) {
        Cafe cafe = currentUserService.getCurrentCafe();
        Shift shift = buildShift(body);
        if (shiftService.shiftNameExists(shift.getName(), cafe)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tên ca đã tồn tại"));
        }
        shift.setCafe(cafe);
        return ResponseEntity.ok(shiftService.saveShiftDto(shift));
    }

    @PutMapping("/api/shifts/{id}")
    public ResponseEntity<?> updateShift(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return shiftService.findShiftById(id).map(existing -> {
            Shift shift = buildShift(body);
            shift.setId(id);
            shift.setCafe(existing.getCafe());
            return ResponseEntity.ok(shiftService.saveShiftDto(shift));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/shifts/{id}")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        shiftService.deleteShiftById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa ca thành công"));
    }

    // ── Schedule ──
    @GetMapping("/api/shift-assignments")
    public ResponseEntity<?> schedule(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStart) {
        Cafe cafe = currentUserService.getCurrentCafe();
        if (weekStart == null) {
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        return ResponseEntity.ok(shiftService.buildWeeklyScheduleDto(weekStart, cafe));
    }

    @PostMapping("/api/shift-assignments")
    public ResponseEntity<?> assign(@RequestBody Map<String, Object> body) {
        Long employeeId = Long.parseLong(body.get("employeeId").toString());
        Long shiftId = Long.parseLong(body.get("shiftId").toString());
        LocalDate date = LocalDate.parse(body.get("date").toString());

        Employee emp = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
        Shift shift = shiftService.findShiftById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Ca không tồn tại"));

        if (shiftService.assignmentExists(emp, date)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nhân viên đã được phân ca ngày này"));
        }

        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployee(emp);
        assignment.setShift(shift);
        assignment.setDate(date);
        if (body.get("note") != null) assignment.setNote(body.get("note").toString());
        return ResponseEntity.ok(shiftService.saveAssignmentDto(assignment));
    }

    @PutMapping("/api/shift-assignments/{id}")
    public ResponseEntity<?> updateAssignment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return shiftService.findAssignmentById(id).map(existing -> {
            if (body.get("employeeId") != null) {
                employeeService.findById(Long.parseLong(body.get("employeeId").toString()))
                        .ifPresent(existing::setEmployee);
            }
            if (body.get("shiftId") != null) {
                shiftService.findShiftById(Long.parseLong(body.get("shiftId").toString()))
                        .ifPresent(existing::setShift);
            }
            if (body.get("date") != null) existing.setDate(LocalDate.parse(body.get("date").toString()));
            if (body.get("note") != null) existing.setNote(body.get("note").toString());
            return ResponseEntity.ok(shiftService.saveAssignmentDto(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/shift-assignments/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        shiftService.deleteAssignmentById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa phân ca thành công"));
    }

    private Shift buildShift(Map<String, Object> body) {
        Shift s = new Shift();
        if (body.get("name") != null) s.setName(body.get("name").toString());
        if (body.get("startTime") != null) s.setStartTime(LocalTime.parse(body.get("startTime").toString()));
        if (body.get("endTime") != null) s.setEndTime(LocalTime.parse(body.get("endTime").toString()));
        if (body.get("description") != null) s.setDescription(body.get("description").toString());
        s.setActive(body.get("active") == null || Boolean.parseBoolean(body.get("active").toString()));
        return s;
    }
}
