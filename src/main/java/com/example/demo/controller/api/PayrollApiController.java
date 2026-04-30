package com.example.demo.controller.api;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Payroll;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollApiController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        Cafe cafe = currentUserService.getCurrentCafe();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(Map.of(
                "payrolls", payrollService.toDtoList(payrollService.findByMonthYear(m, y, cafe)),
                "totalSalary", payrollService.sumNetSalary(m, y, cafe),
                "month", m,
                "year", y));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return payrollService.findById(id)
                .map(payrollService::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam int month, @RequestParam int year) {
        Cafe cafe = currentUserService.getCurrentCafe();
        var list = payrollService.generatePayrollForMonth(month, year, cafe);
        return ResponseEntity.ok(Map.of("message", "Đã tạo " + list.size() + " phiếu lương", "count", list.size()));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Payroll payroll = buildPayroll(body);
        Long employeeId = Long.parseLong(body.get("employeeId").toString());
        employeeService.findById(employeeId).ifPresent(emp -> {
            payroll.setEmployee(emp);
            if (payroll.getBaseSalary() == null || payroll.getBaseSalary().signum() == 0) {
                payroll.setBaseSalary(emp.getSalary() != null ? emp.getSalary() : BigDecimal.ZERO);
            }
        });
        payroll.recalculate();
        return ResponseEntity.ok(payrollService.toDto(payrollService.save(payroll)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return payrollService.findById(id).map(existing -> {
            Payroll payroll = buildPayroll(body);
            payroll.setId(id);
            payroll.setEmployee(existing.getEmployee());
            payroll.recalculate();
            return ResponseEntity.ok(payrollService.toDto(payrollService.save(payroll)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        payrollService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        payrollService.approvePayroll(id);
        return ResponseEntity.ok(Map.of("message", "Đã duyệt phiếu lương"));
    }

    @PostMapping("/{id}/paid")
    public ResponseEntity<?> markPaid(@PathVariable Long id) {
        payrollService.markPaid(id);
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu đã thanh toán"));
    }

    @PostMapping("/approve-all")
    public ResponseEntity<?> approveAll(@RequestParam int month, @RequestParam int year) {
        Cafe cafe = currentUserService.getCurrentCafe();
        payrollService.approveAll(month, year, cafe);
        return ResponseEntity.ok(Map.of("message", "Đã duyệt tất cả phiếu lương"));
    }

    private Payroll buildPayroll(Map<String, Object> body) {
        Payroll p = new Payroll();
        if (body.get("month") != null) p.setMonth(Integer.parseInt(body.get("month").toString()));
        if (body.get("year") != null) p.setYear(Integer.parseInt(body.get("year").toString()));
        if (body.get("baseSalary") != null) p.setBaseSalary(new BigDecimal(body.get("baseSalary").toString()));
        if (body.get("standardWorkDays") != null) p.setStandardWorkDays(Integer.parseInt(body.get("standardWorkDays").toString()));
        if (body.get("actualWorkDays") != null) p.setActualWorkDays(Integer.parseInt(body.get("actualWorkDays").toString()));
        if (body.get("paidLeaveDays") != null) p.setPaidLeaveDays(Integer.parseInt(body.get("paidLeaveDays").toString()));
        if (body.get("overtimeHours") != null) p.setOvertimeHours(new BigDecimal(body.get("overtimeHours").toString()));
        if (body.get("overtimeRate") != null) p.setOvertimeRate(new BigDecimal(body.get("overtimeRate").toString()));
        if (body.get("allowance") != null) p.setAllowance(new BigDecimal(body.get("allowance").toString()));
        if (body.get("deduction") != null) p.setDeduction(new BigDecimal(body.get("deduction").toString()));
        if (body.get("note") != null) p.setNote(body.get("note").toString());
        return p;
    }
}
