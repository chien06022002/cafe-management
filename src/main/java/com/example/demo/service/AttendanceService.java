package com.example.demo.service;

import com.example.demo.dto.attendance.AttendanceDto;
import com.example.demo.dto.common.EmployeeSummaryDto;
import com.example.demo.entity.Attendance;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> findByDate(LocalDate date, Cafe cafe) {
        if (cafe == null) return attendanceRepository.findByDate(date);
        return attendanceRepository.findByCafeAndDate(cafe, date);
    }

    public List<Attendance> findByEmployee(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId).orElseThrow();
        return attendanceRepository.findByEmployee(emp);
    }

    public List<Attendance> findByDateRange(LocalDate start, LocalDate end, Cafe cafe) {
        if (cafe == null) return attendanceRepository.findByDateBetween(start, end);
        return attendanceRepository.findByCafeAndDateBetween(cafe, start, end);
    }

    public Optional<Attendance> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    public void deleteById(Long id) {
        attendanceRepository.deleteById(id);
    }

    public long countPresentToday(Cafe cafe) {
        if (cafe == null) return attendanceRepository.countPresentByDate(LocalDate.now());
        return attendanceRepository.countPresentByCafeAndDate(cafe, LocalDate.now());
    }

    public long countAbsentToday(Cafe cafe) {
        if (cafe == null) return attendanceRepository.countAbsentByDate(LocalDate.now());
        return attendanceRepository.countAbsentByCafeAndDate(cafe, LocalDate.now());
    }

    public boolean existsByEmployeeAndDate(Employee employee, LocalDate date) {
        return attendanceRepository.findByEmployeeAndDate(employee, date).isPresent();
    }

    // ── DTO mapper ──

    public AttendanceDto toDto(Attendance a) {
        if (a == null) return null;
        Employee emp = a.getEmployee();
        EmployeeSummaryDto empDto = emp == null ? null : new EmployeeSummaryDto(
                emp.getId(),
                emp.getEmployeeCode(),
                emp.getFullName(),
                emp.getPosition() != null ? emp.getPosition() : "",
                emp.getAvatarUrl() != null ? emp.getAvatarUrl() : ""
        );
        return new AttendanceDto(
                a.getId(),
                empDto,
                a.getDate() != null ? a.getDate().toString() : "",
                a.getCheckIn() != null ? a.getCheckIn().toString() : null,
                a.getCheckOut() != null ? a.getCheckOut().toString() : null,
                a.getStatus(),
                a.getNote()
        );
    }

    public java.util.List<AttendanceDto> toDtoList(java.util.List<Attendance> list) {
        return list.stream().map(this::toDto).toList();
    }
}
