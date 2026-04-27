package com.example.demo.controller;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Payroll;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Model model) {

        Cafe cafe = currentUserService.getCurrentCafe();
        int m = (month != null) ? month : LocalDate.now().getMonthValue();
        int y = (year != null) ? year : LocalDate.now().getYear();

        model.addAttribute("payrolls", payrollService.findByMonthYear(m, y, cafe));
        model.addAttribute("selectedMonth", m);
        model.addAttribute("selectedYear", y);
        model.addAttribute("totalSalary", payrollService.sumNetSalary(m, y, cafe));
        return "payroll/list";
    }

    @PostMapping("/generate")
    public String generate(@RequestParam int month, @RequestParam int year, RedirectAttributes ra) {
        Cafe cafe = currentUserService.getCurrentCafe();
        var list = payrollService.generatePayrollForMonth(month, year, cafe);
        ra.addFlashAttribute("success",
                "Đã tạo " + list.size() + " phiếu lương tháng " + month + "/" + year);
        return "redirect:/payroll?month=" + month + "&year=" + year;
    }

    @PostMapping("/approve-all")
    public String approveAll(@RequestParam int month, @RequestParam int year, RedirectAttributes ra) {
        Cafe cafe = currentUserService.getCurrentCafe();
        payrollService.approveAll(month, year, cafe);
        ra.addFlashAttribute("success", "Đã duyệt tất cả phiếu lương!");
        return "redirect:/payroll?month=" + month + "&year=" + year;
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam int month, @RequestParam int year,
                          RedirectAttributes ra) {
        payrollService.approvePayroll(id);
        ra.addFlashAttribute("success", "Duyệt phiếu lương thành công!");
        return "redirect:/payroll?month=" + month + "&year=" + year;
    }

    @PostMapping("/{id}/paid")
    public String markPaid(@PathVariable Long id, @RequestParam int month, @RequestParam int year,
                            RedirectAttributes ra) {
        payrollService.markPaid(id);
        ra.addFlashAttribute("success", "Đã đánh dấu đã thanh toán!");
        return "redirect:/payroll?month=" + month + "&year=" + year;
    }

    @GetMapping("/{id}/edit")
    public String showEdit(@PathVariable Long id, Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        Payroll payroll = payrollService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phiếu lương không tồn tại"));
        model.addAttribute("payroll", payroll);
        model.addAttribute("employees", employeeService.findAll(cafe));
        return "payroll/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Payroll payroll,
                          RedirectAttributes ra) {
        Payroll existing = payrollService.findById(id).orElseThrow();
        payroll.setId(id);
        payroll.setEmployee(existing.getEmployee());
        payrollService.save(payroll);
        ra.addFlashAttribute("success", "Cập nhật phiếu lương thành công!");
        return "redirect:/payroll?month=" + existing.getMonth() + "&year=" + existing.getYear();
    }

    @GetMapping("/new")
    public String showCreate(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        Payroll payroll = new Payroll();
        payroll.setMonth(LocalDate.now().getMonthValue());
        payroll.setYear(LocalDate.now().getYear());
        model.addAttribute("payroll", payroll);
        model.addAttribute("employees", employeeService.findActive(cafe));
        return "payroll/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Payroll payroll,
                          @RequestParam Long employeeId,
                          RedirectAttributes ra) {
        employeeService.findById(employeeId).ifPresent(emp -> {
            payroll.setEmployee(emp);
            if (payroll.getBaseSalary() == null || payroll.getBaseSalary().signum() == 0) {
                payroll.setBaseSalary(emp.getSalary() != null ? emp.getSalary() : java.math.BigDecimal.ZERO);
            }
        });
        payrollService.save(payroll);
        ra.addFlashAttribute("success", "Tạo phiếu lương thành công!");
        return "redirect:/payroll?month=" + payroll.getMonth() + "&year=" + payroll.getYear();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam int month, @RequestParam int year,
                          RedirectAttributes ra) {
        payrollService.deleteById(id);
        ra.addFlashAttribute("success", "Xóa phiếu lương thành công!");
        return "redirect:/payroll?month=" + month + "&year=" + year;
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id, Model model) {
        Payroll payroll = payrollService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phiếu lương không tồn tại"));
        model.addAttribute("payroll", payroll);
        return "payroll/detail";
    }
}
