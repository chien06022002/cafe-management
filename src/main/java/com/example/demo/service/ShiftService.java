package com.example.demo.service;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.ShiftAssignmentRepository;
import com.example.demo.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;

    // ---- Shifts ----
    public List<Shift> findAllShifts(Cafe cafe) {
        if (cafe == null) return shiftRepository.findAll();
        return shiftRepository.findByCafe(cafe);
    }

    public List<Shift> findActiveShifts(Cafe cafe) {
        if (cafe == null) return shiftRepository.findByActive(true);
        return shiftRepository.findByCafeAndActive(cafe, true);
    }

    public Optional<Shift> findShiftById(Long id) {
        return shiftRepository.findById(id);
    }

    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    public void deleteShiftById(Long id) {
        shiftRepository.deleteById(id);
    }

    public boolean shiftNameExists(String name, Cafe cafe) {
        if (cafe == null) return shiftRepository.existsByName(name);
        return shiftRepository.existsByNameAndCafe(name, cafe);
    }

    // ---- Assignments ----
    public List<ShiftAssignment> findAssignmentsByDate(LocalDate date) {
        return assignmentRepository.findByDate(date);
    }

    public List<ShiftAssignment> findAssignmentsByDateRange(LocalDate start, LocalDate end, Cafe cafe) {
        if (cafe == null) return assignmentRepository.findByDateRangeWithDetails(start, end);
        return assignmentRepository.findByCafeAndDateRangeWithDetails(cafe, start, end);
    }

    public List<ShiftAssignment> findAssignmentsByEmployee(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId).orElseThrow();
        return assignmentRepository.findByEmployee(emp);
    }

    public Optional<ShiftAssignment> findAssignmentById(Long id) {
        return assignmentRepository.findById(id);
    }

    public ShiftAssignment saveAssignment(ShiftAssignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public void deleteAssignmentById(Long id) {
        assignmentRepository.deleteById(id);
    }

    public boolean assignmentExists(Employee employee, LocalDate date) {
        return assignmentRepository.findByEmployeeAndDate(employee, date).isPresent();
    }

    /** Lấy lịch làm việc theo tuần (từ ngày thứ Hai) */
    public List<ShiftAssignment> findWeeklySchedule(LocalDate weekStart, Cafe cafe) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return findAssignmentsByDateRange(weekStart, weekEnd, cafe);
    }
}
