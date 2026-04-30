package com.example.demo.controller.api;

import com.example.demo.dto.employee.EmployeeDto;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeApiController {

    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;
    private final EmployeeRepository employeeRepository;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String keyword) {
        Cafe cafe = currentUserService.getCurrentCafe();
        return ResponseEntity.ok(employeeService.toDtoList(employeeService.search(keyword, cafe)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(employeeService::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Employee employee) {
        Cafe cafe = currentUserService.getCurrentCafe();
        if (employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank()) {
            employee.setEmployeeCode(employeeService.generateNextCode(cafe));
        }
        if (employeeService.existsByCode(employee.getEmployeeCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã nhân viên đã tồn tại"));
        }
        employee.setCafe(cafe);
        Employee saved = employeeService.createWithAccount(employee);
        boolean accountCreated = saved.getUser() != null;
        return ResponseEntity.ok(Map.of(
                "employee", employeeService.toDto(saved),
                "accountCreated", accountCreated,
                "loginUsername", accountCreated ? saved.getUser().getUsername() : "",
                "defaultPassword", accountCreated ? "cafe123" : ""
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Employee employee) {
        return employeeService.findById(id).map(existing -> {
            employee.setId(id);
            employee.setCafe(existing.getCafe());
            employee.setUser(existing.getUser());
            return ResponseEntity.ok(employeeService.toDto(employeeService.save(employee)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        employeeService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }

    // Face registration endpoints
    @PostMapping("/{id}/face")
    public ResponseEntity<?> saveFace(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return employeeRepository.findById(id).map(emp -> {
            emp.setFaceDescriptor(body.get("descriptor").toString());
            employeeRepository.save(emp);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đăng ký khuôn mặt thành công"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/face")
    public ResponseEntity<?> deleteFace(@PathVariable Long id) {
        employeeRepository.findById(id).ifPresent(emp -> {
            emp.setFaceDescriptor(null);
            employeeRepository.save(emp);
        });
        return ResponseEntity.ok(Map.of("message", "Đã xóa dữ liệu khuôn mặt"));
    }
}
