package com.example.demo.service;

import com.example.demo.dto.common.EmployeeSummaryDto;
import com.example.demo.dto.shift.ShiftAssignmentDto;
import com.example.demo.dto.shift.ShiftDto;
import com.example.demo.dto.shift.WeeklyScheduleDto;
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
import java.util.ArrayList;
import java.util.Comparator;
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

    public List<ShiftDto> findAllShiftDtos(Cafe cafe) {
        return findAllShifts(cafe).stream().map(this::toShiftDto).toList();
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

    public ShiftDto saveShiftDto(Shift shift) {
        return toShiftDto(saveShift(shift));
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

    public List<ShiftAssignment> findAssignmentsByEmployeeAndDateRange(Employee employee, LocalDate start, LocalDate end) {
        return assignmentRepository.findByEmployeeAndDateBetweenWithDetails(employee, start, end);
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

    public ShiftAssignmentDto saveAssignmentDto(ShiftAssignment assignment) {
        return toShiftAssignmentDto(saveAssignment(assignment));
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

    public WeeklyScheduleDto buildWeeklyScheduleDto(LocalDate weekStart, Cafe cafe) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<ShiftAssignment> assignments = findWeeklySchedule(weekStart, cafe);

        // Always show ALL active shifts for the cafe, regardless of assignments this week.
        // Shifts with no assignments simply show as empty columns in the grid.
        List<ShiftDto> weekShifts = findActiveShifts(cafe).stream()
                .sorted(Comparator.comparing(Shift::getStartTime))
                .map(this::toShiftDto)
                .toList();


        List<String> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDays.add(weekStart.plusDays(i).toString());
        }

        List<Employee> activeEmployees = (cafe == null)
                ? employeeRepository.findByStatus("ACTIVE")
                : employeeRepository.findByCafeAndStatus(cafe, "ACTIVE");

        return new WeeklyScheduleDto(
                assignments.stream().map(this::toShiftAssignmentDto).toList(),
                weekShifts,
                activeEmployees.stream().map(this::toEmployeeSummaryDto).toList(),
                weekStart.toString(),
                weekEnd.toString(),
                weekDays
        );
    }

    public ShiftDto toShiftDto(Shift shift) {
        if (shift == null) return null;
        return new ShiftDto(
                shift.getId(),
                shift.getName(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getDescription(),
                shift.isActive()
        );
    }

    public EmployeeSummaryDto toEmployeeSummaryDto(Employee employee) {
        if (employee == null) return null;
        return new EmployeeSummaryDto(
                employee.getId(),
                employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "",
                employee.getFullName(),
                employee.getPosition() != null ? employee.getPosition() : "",
                employee.getAvatarUrl() != null ? employee.getAvatarUrl() : ""
        );
    }

    public ShiftAssignmentDto toShiftAssignmentDto(ShiftAssignment assignment) {
        if (assignment == null) return null;
        return new ShiftAssignmentDto(
                assignment.getId(),
                assignment.getDate(),
                assignment.getNote(),
                toShiftDto(assignment.getShift()),
                toEmployeeSummaryDto(assignment.getEmployee())
        );
    }
}
