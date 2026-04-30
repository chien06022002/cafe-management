package com.example.demo.controller.api;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceApiController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;
    private final AttendanceRepository attendanceRepository;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Long employeeId) {
        Cafe cafe = currentUserService.getCurrentCafe();
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        var result = employeeId != null
                ? attendanceService.toDtoList(attendanceService.findByEmployee(employeeId))
                : attendanceService.toDtoList(attendanceService.findByDate(selectedDate, cafe));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return attendanceRepository.findById(id)
                .map(attendanceService::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long employeeId = Long.parseLong(body.get("employeeId").toString());
        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        LocalDate date = LocalDate.parse(body.get("date").toString());
        if (attendanceService.existsByEmployeeAndDate(employee, date)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nhân viên đã có dữ liệu chấm công ngày này"));
        }

        Attendance a = new Attendance();
        a.setEmployee(employee);
        a.setDate(date);
        a.setStatus(body.getOrDefault("status", "PRESENT").toString());
        if (body.get("checkIn") != null) a.setCheckIn(java.time.LocalTime.parse(body.get("checkIn").toString()));
        if (body.get("checkOut") != null) a.setCheckOut(java.time.LocalTime.parse(body.get("checkOut").toString()));
        if (body.get("note") != null) a.setNote(body.get("note").toString());
        return ResponseEntity.ok(attendanceService.toDto(attendanceRepository.save(a)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return attendanceRepository.findById(id).map(a -> {
            if (body.get("status") != null) a.setStatus(body.get("status").toString());
            if (body.get("checkIn") != null) a.setCheckIn(java.time.LocalTime.parse(body.get("checkIn").toString()));
            if (body.get("checkOut") != null) a.setCheckOut(java.time.LocalTime.parse(body.get("checkOut").toString()));
            if (body.get("note") != null) a.setNote(body.get("note").toString());
            return ResponseEntity.ok(attendanceService.toDto(attendanceRepository.save(a)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        attendanceRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }
}
