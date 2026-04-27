package com.example.demo.service;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Payroll;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public List<Payroll> findByMonthYear(int month, int year, Cafe cafe) {
        if (cafe == null) return payrollRepository.findByMonthAndYearWithEmployee(month, year);
        return payrollRepository.findByCafeAndMonthAndYearWithEmployee(cafe, month, year);
    }

    public Optional<Payroll> findById(Long id) {
        return payrollRepository.findById(id);
    }

    public List<Payroll> findByEmployee(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId).orElseThrow();
        return payrollRepository.findByEmployee(emp);
    }

    public Payroll save(Payroll payroll) {
        payroll.recalculate();
        return payrollRepository.save(payroll);
    }

    public void deleteById(Long id) {
        payrollRepository.deleteById(id);
    }

    /**
     * Tự động tạo bảng lương cho tất cả nhân viên ACTIVE trong tháng.
     * Đếm ngày công từ bảng attendance.
     */
    public List<Payroll> generatePayrollForMonth(int month, int year, Cafe cafe) {
        List<Employee> employees = (cafe == null)
                ? employeeRepository.findByStatus("ACTIVE")
                : employeeRepository.findByCafeAndStatus(cafe, "ACTIVE");
        List<Payroll> generated = new ArrayList<>();

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = YearMonth.of(year, month).atEndOfMonth();

        for (Employee emp : employees) {
            // Bỏ qua nếu đã có bảng lương
            if (payrollRepository.findByEmployeeAndMonthAndYear(emp, month, year).isPresent()) {
                continue;
            }

            // Đếm ngày PRESENT + LATE + HALF_DAY từ bảng chấm công
            List<Attendance> attendances = attendanceRepository
                    .findByEmployeeAndDateBetween(emp, monthStart, monthEnd);

            int presentDays = 0;
            int paidLeaveDays = 0;
            for (Attendance a : attendances) {
                switch (a.getStatus()) {
                    case "PRESENT", "LATE" -> presentDays++;
                    case "HALF_DAY" -> presentDays++;
                    case "LEAVE" -> paidLeaveDays++;
                }
            }

            Payroll payroll = new Payroll();
            payroll.setEmployee(emp);
            payroll.setMonth(month);
            payroll.setYear(year);
            payroll.setBaseSalary(emp.getSalary() != null ? emp.getSalary() : BigDecimal.ZERO);
            payroll.setActualWorkDays(presentDays);
            payroll.setPaidLeaveDays(paidLeaveDays);
            payroll.recalculate();
            generated.add(payrollRepository.save(payroll));
        }
        return generated;
    }

    public void approvePayroll(Long id) {
        payrollRepository.findById(id).ifPresent(p -> {
            p.setStatus("APPROVED");
            payrollRepository.save(p);
        });
    }

    public void markPaid(Long id) {
        payrollRepository.findById(id).ifPresent(p -> {
            p.setStatus("PAID");
            p.setPaidDate(LocalDate.now());
            payrollRepository.save(p);
        });
    }

    public void approveAll(int month, int year, Cafe cafe) {
        List<Payroll> pending = (cafe == null)
                ? payrollRepository.findByMonthAndYearAndStatus(month, year, "PENDING")
                : payrollRepository.findByCafeAndMonthAndYearAndStatus(cafe, month, year, "PENDING");
        pending.forEach(p -> { p.setStatus("APPROVED"); payrollRepository.save(p); });
    }

    public BigDecimal sumNetSalary(int month, int year, Cafe cafe) {
        List<Payroll> list = (cafe == null)
                ? payrollRepository.findByMonthAndYear(month, year)
                : payrollRepository.findByCafeAndMonthAndYearWithEmployee(cafe, month, year);
        return list.stream()
                .map(Payroll::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
