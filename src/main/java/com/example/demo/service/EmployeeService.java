package com.example.demo.service;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

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
}
