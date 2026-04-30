package com.example.demo.controller.api;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Payroll;
import com.example.demo.entity.User;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.PayrollRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PayrollService;
import com.example.demo.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileApiController {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final PayrollService payrollService;
    private final ShiftService shiftService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** GET /api/profile/me - lấy thông tin cá nhân của nhân viên hiện tại */
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.getEmployee() == null) {
            return ResponseEntity.ok(Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
            ));
        }
        Employee emp = user.getEmployee();
        Map<String, Object> result = new HashMap<>();
        result.put("id", emp.getId());
        result.put("employeeCode", emp.getEmployeeCode());
        result.put("fullName", emp.getFullName());
        result.put("phone", emp.getPhone() != null ? emp.getPhone() : "");
        result.put("email", emp.getEmail() != null ? emp.getEmail() : "");
        result.put("address", emp.getAddress() != null ? emp.getAddress() : "");
        result.put("position", emp.getPosition() != null ? emp.getPosition() : "");
        result.put("department", emp.getDepartment() != null ? emp.getDepartment() : "");
        result.put("hireDate", emp.getHireDate() != null ? emp.getHireDate().toString() : "");
        result.put("status", emp.getStatus());
        result.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() :
                (emp.getAvatarUrl() != null ? emp.getAvatarUrl() : ""));
        result.put("username", user.getUsername());
        return ResponseEntity.ok(result);
    }

    /** PUT /api/profile/me - cập nhật thông tin cá nhân */
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> body, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.getEmployee() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin nhân viên"));
        }
        Employee emp = user.getEmployee();
        if (body.get("fullName") != null) emp.setFullName(body.get("fullName").toString());
        if (body.get("phone") != null) emp.setPhone(body.get("phone").toString());
        if (body.get("address") != null) emp.setAddress(body.get("address").toString());
        // Email update: also update user email
        if (body.get("email") != null) {
            emp.setEmail(body.get("email").toString());
            user.setEmail(body.get("email").toString());
            userRepository.save(user);
        }
        employeeRepository.save(emp);
        return ResponseEntity.ok(Map.of("message", "Cập nhật thông tin thành công"));
    }

    /** POST /api/profile/me/avatar - upload ảnh đại diện */
    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication auth) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File không hợp lệ"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ chấp nhận file ảnh"));
        }

        // Create upload directory
        Path uploadPath = Paths.get(uploadDir, "avatars");
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = auth.getName().replaceAll("[^a-zA-Z0-9._-]", "_") + "_" + System.currentTimeMillis() + ext;
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        String avatarUrl = "/uploads/avatars/" + filename;

        // Update user avatarUrl
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        // Also update employee avatarUrl if linked
        if (user.getEmployee() != null) {
            user.getEmployee().setAvatarUrl(avatarUrl);
            employeeRepository.save(user.getEmployee());
        }

        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl, "message", "Ảnh đại diện đã được cập nhật"));
    }

    /** GET /api/profile/my-shifts?weekStart=yyyy-MM-dd - lịch làm việc của nhân viên */
    @GetMapping("/my-shifts")
    public ResponseEntity<?> getMyShifts(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStart,
            Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.getEmployee() == null) {
            return ResponseEntity.ok(Map.of("assignments", List.of()));
        }
        if (weekStart == null) {
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        LocalDate weekEnd = weekStart.plusDays(6);
        var assignments = shiftService.findAssignmentsByEmployeeAndDateRange(user.getEmployee(), weekStart, weekEnd)
                .stream()
                .map(shiftService::toShiftAssignmentDto)
                .toList();

        List<String> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) weekDays.add(weekStart.plusDays(i).toString());

        return ResponseEntity.ok(Map.of(
                "assignments", assignments,
                "weekStart", weekStart.toString(),
                "weekEnd", weekEnd.toString(),
                "weekDays", weekDays
        ));
    }

    /** GET /api/profile/my-payroll?month=&year= - bảng lương của nhân viên */
    @GetMapping("/my-payroll")
    public ResponseEntity<?> getMyPayroll(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.getEmployee() == null) {
            return ResponseEntity.ok(Map.of("payrolls", List.of()));
        }
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        List<Payroll> payrolls = payrollRepository.findByEmployee(user.getEmployee());
        // Filter by month/year or return all
        if (month != null || year != null) {
            final int fm = m, fy = y;
            payrolls = payrolls.stream()
                    .filter(p -> p.getMonth() == fm && p.getYear() == fy)
                    .toList();
        }
        return ResponseEntity.ok(Map.of("payrolls", payrollService.toDtoList(payrolls), "month", m, "year", y));
    }
}

