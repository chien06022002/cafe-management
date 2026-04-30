package com.example.demo.service;

import com.example.demo.dto.common.EmployeeSummaryDto;
import com.example.demo.dto.employee.EmployeeDto;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Employee> findAll(Cafe cafe) {
        if (cafe == null) return employeeRepository.findAll();
        return employeeRepository.findByCafe(cafe);
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Tạo nhân viên mới và tự động tạo tài khoản đăng nhập.
     * Username = email (gmail), password mặc định = "cafe123", mustChangePassword = true.
     */
    public Employee createWithAccount(Employee employee) {
        Employee saved = employeeRepository.save(employee);

        // Auto-create user account if email provided and not already exists
        String email = employee.getEmail();
        if (email != null && !email.isBlank() && !userRepository.existsByUsername(email)) {
            Role staffRole = roleRepository.findByName("ROLE_STAFF")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_STAFF")));

            User user = new User();
            user.setUsername(email);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("cafe123"));
            user.setMustChangePassword(true);
            user.setEnabled(true);
            user.setRoles(Set.of(staffRole));
            user.setCafe(employee.getCafe());
            User savedUser = userRepository.save(user);

            saved.setUser(savedUser);
            saved = employeeRepository.save(saved);
        }
        return saved;
    }

    public void deleteById(Long id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> findActive(Cafe cafe) {
        if (cafe == null) return employeeRepository.findByStatus("ACTIVE");
        return employeeRepository.findByCafeAndStatus(cafe, "ACTIVE");
    }

    public List<Employee> search(String keyword, Cafe cafe) {
        if (keyword == null || keyword.isBlank()) return findAll(cafe);
        if (cafe == null) {
            return employeeRepository.findByFullNameContainingIgnoreCaseOrEmployeeCodeContainingIgnoreCase(keyword, keyword);
        }
        return employeeRepository.searchByCafe(cafe, keyword);
    }

    public boolean existsByCode(String code) {
        return employeeRepository.existsByEmployeeCode(code);
    }

    public String generateNextCode(Cafe cafe) {
        long count = (cafe != null ? employeeRepository.countByCafe(cafe) : employeeRepository.count()) + 1;
        return String.format("NV%04d", count);
    }

    // ── DTO mappers ──

    public EmployeeDto toDto(Employee emp) {
        if (emp == null) return null;
        return new EmployeeDto(
                emp.getId(),
                emp.getEmployeeCode(),
                emp.getFullName(),
                emp.getPhone() != null ? emp.getPhone() : "",
                emp.getEmail() != null ? emp.getEmail() : "",
                emp.getAddress() != null ? emp.getAddress() : "",
                emp.getPosition() != null ? emp.getPosition() : "",
                emp.getDepartment() != null ? emp.getDepartment() : "",
                emp.getHireDate() != null ? emp.getHireDate().toString() : "",
                emp.getSalary(),
                emp.getStatus(),
                emp.getAvatarUrl() != null ? emp.getAvatarUrl() : "",
                emp.getFaceDescriptor() != null,
                emp.getUser() != null ? emp.getUser().getId() : null
        );
    }

    public EmployeeSummaryDto toSummaryDto(Employee emp) {
        if (emp == null) return null;
        return new EmployeeSummaryDto(
                emp.getId(),
                emp.getEmployeeCode(),
                emp.getFullName(),
                emp.getPosition() != null ? emp.getPosition() : "",
                emp.getAvatarUrl() != null ? emp.getAvatarUrl() : ""
        );
    }

    public java.util.List<EmployeeDto> toDtoList(java.util.List<Employee> employees) {
        return employees.stream().map(this::toDto).toList();
    }
}
