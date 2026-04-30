package com.example.demo.controller.api;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.CafeRepository;
import com.example.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
public class FaceApiController {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final CafeRepository cafeRepository;

    /** Returns face descriptors for all active employees in a cafe (public) */
    @GetMapping("/{cafeId}/descriptors")
    public ResponseEntity<List<Map<String, Object>>> getDescriptors(@PathVariable Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId).orElse(null);
        if (cafe == null) return ResponseEntity.badRequest().build();
        List<Map<String, Object>> result = employeeRepository.findByCafeAndStatus(cafe, "ACTIVE").stream()
                .filter(e -> e.getFaceDescriptor() != null && !e.getFaceDescriptor().isEmpty())
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("employeeId", e.getId());
                    m.put("employeeCode", e.getEmployeeCode());
                    m.put("fullName", e.getFullName());
                    m.put("position", e.getPosition());
                    m.put("descriptor", e.getFaceDescriptor());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** Record attendance via face recognition (public kiosk endpoint) */
    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> record(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long employeeId = Long.parseLong(body.get("employeeId").toString());
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

            LocalDate today = LocalDate.now();
            if (attendanceRepository.findByEmployeeAndDate(employee, today).isPresent()) {
                res.put("success", false);
                res.put("alreadyCheckedIn", true);
                res.put("message", employee.getFullName() + " đã chấm công hôm nay rồi!");
                return ResponseEntity.ok(res);
            }

            LocalTime now = LocalTime.now();
            Attendance a = new Attendance();
            a.setEmployee(employee);
            a.setDate(today);
            a.setCheckIn(now);
            a.setStatus(now.isBefore(LocalTime.of(8, 30)) ? "PRESENT" : "LATE");
            a.setNote("Chấm công bằng khuôn mặt");
            attendanceRepository.save(a);

            res.put("success", true);
            res.put("employeeName", employee.getFullName());
            res.put("employeeCode", employee.getEmployeeCode());
            res.put("position", employee.getPosition());
            res.put("checkInTime", now.toString().substring(0, 5));
            res.put("status", a.getStatus());
            res.put("message", "Chấm công thành công!");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** Get cafe info for kiosk (public) */
    @GetMapping("/{cafeId}/info")
    public ResponseEntity<?> getCafeInfo(@PathVariable Long cafeId) {
        return cafeRepository.findById(cafeId).map(cafe -> {
            long count = employeeRepository.findByCafeAndStatus(cafe, "ACTIVE").stream()
                    .filter(e -> e.getFaceDescriptor() != null && !e.getFaceDescriptor().isEmpty())
                    .count();
            return ResponseEntity.ok(Map.of(
                    "cafeId", cafe.getId(),
                    "cafeName", cafe.getName(),
                    "registeredCount", count));
        }).orElse(ResponseEntity.notFound().build());
    }
}
