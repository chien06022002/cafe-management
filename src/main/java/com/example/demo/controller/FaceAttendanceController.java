package com.example.demo.controller;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.CafeRepository;
import com.example.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class FaceAttendanceController {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final CafeRepository cafeRepository;

    // ==========================================
    // ĐĂNG KÝ KHUÔN MẶT (yêu cầu đăng nhập)
    // ==========================================

    @GetMapping("/employees/{id}/face-register")
    public String showFaceRegister(@PathVariable Long id, Model model) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
        model.addAttribute("employee", employee);
        model.addAttribute("hasFace", employee.getFaceDescriptor() != null
                && !employee.getFaceDescriptor().isEmpty());
        return "attendance/face-register";
    }

    @PostMapping("/employees/{id}/face-register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveFaceDescriptor(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

            String descriptor = body.get("descriptor").toString();
            employee.setFaceDescriptor(descriptor);
            employeeRepository.save(employee);

            response.put("success", true);
            response.put("message", "Đăng ký khuôn mặt thành công cho " + employee.getFullName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/employees/{id}/face-delete")
    public String deleteFaceDescriptor(@PathVariable Long id, RedirectAttributes ra) {
        employeeRepository.findById(id).ifPresent(emp -> {
            emp.setFaceDescriptor(null);
            employeeRepository.save(emp);
        });
        ra.addFlashAttribute("success", "Đã xóa dữ liệu khuôn mặt!");
        return "redirect:/employees/" + id;
    }

    // ==========================================
    // KIOSK CHẤM CÔNG (public, không cần login, theo quán)
    // ==========================================

    /** Kiosk cho một quán cụ thể: /face-checkin/{cafeId} */
    @GetMapping("/face-checkin/{cafeId}")
    public String kioskPage(@PathVariable Long cafeId, Model model) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("Quán không tồn tại"));
        long registeredCount = employeeRepository.findByCafeAndStatus(cafe, "ACTIVE").stream()
                .filter(e -> e.getFaceDescriptor() != null && !e.getFaceDescriptor().isEmpty())
                .count();
        model.addAttribute("registeredCount", registeredCount);
        model.addAttribute("cafeName", cafe.getName());
        model.addAttribute("cafeId", cafeId);
        return "attendance/face-checkin";
    }

    /** Backward compat: /face-checkin redirects to default or shows cafe list */
    @GetMapping("/face-checkin")
    public String kioskDefault(Model model) {
        List<Cafe> activeCafes = cafeRepository.findByActive(true);
        if (activeCafes.size() == 1) {
            return "redirect:/face-checkin/" + activeCafes.get(0).getId();
        }
        model.addAttribute("cafes", activeCafes);
        return "attendance/cafe-select";
    }

    /**
     * Trả về descriptors của nhân viên cho một quán cụ thể.
     */
    @GetMapping("/face-checkin/{cafeId}/descriptors")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getDescriptors(@PathVariable Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId).orElse(null);
        if (cafe == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Map<String, Object>> result = employeeRepository
                .findByCafeAndStatus(cafe, "ACTIVE").stream()
                .filter(e -> e.getFaceDescriptor() != null && !e.getFaceDescriptor().isEmpty())
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("employeeId", e.getId());
                    m.put("employeeCode", e.getEmployeeCode());
                    m.put("fullName", e.getFullName());
                    m.put("position", e.getPosition());
                    m.put("department", e.getDepartment());
                    m.put("descriptor", e.getFaceDescriptor());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** Legacy descriptors endpoint (returns all) */
    @GetMapping("/face-checkin/descriptors")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllDescriptors() {
        List<Map<String, Object>> result = employeeRepository.findAll().stream()
                .filter(e -> e.getFaceDescriptor() != null && !e.getFaceDescriptor().isEmpty()
                        && "ACTIVE".equals(e.getStatus()))
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("employeeId", e.getId());
                    m.put("employeeCode", e.getEmployeeCode());
                    m.put("fullName", e.getFullName());
                    m.put("position", e.getPosition());
                    m.put("department", e.getDepartment());
                    m.put("descriptor", e.getFaceDescriptor());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Ghi nhận chấm công sau khi xác nhận khuôn mặt thành công.
     * CSRF bị tắt cho endpoint này (public kiosk).
     */
    @PostMapping("/face-checkin/record")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordAttendance(
            @RequestBody Map<String, Object> body) {

        Map<String, Object> response = new HashMap<>();
        try {
            Long employeeId = Long.parseLong(body.get("employeeId").toString());
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

            LocalDate today = LocalDate.now();

            // Kiểm tra đã chấm công hôm nay chưa
            if (attendanceRepository.findByEmployeeAndDate(employee, today).isPresent()) {
                response.put("success", false);
                response.put("alreadyCheckedIn", true);
                response.put("message", employee.getFullName() + " đã chấm công hôm nay rồi!");
                response.put("employeeName", employee.getFullName());
                return ResponseEntity.ok(response);
            }

            // Tạo bản ghi chấm công
            LocalTime now = LocalTime.now();
            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setDate(today);
            attendance.setCheckIn(now);
            attendance.setStatus(now.isBefore(LocalTime.of(8, 30)) ? "PRESENT" : "LATE");
            attendance.setNote("Chấm công bằng khuôn mặt");
            attendanceRepository.save(attendance);

            response.put("success", true);
            response.put("employeeName", employee.getFullName());
            response.put("employeeCode", employee.getEmployeeCode());
            response.put("position", employee.getPosition());
            response.put("checkInTime", now.toString().substring(0, 5));
            response.put("status", attendance.getStatus());
            response.put("message", "Chấm công thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
