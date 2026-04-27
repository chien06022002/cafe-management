package com.example.demo.controller;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        model.addAttribute("employees", employeeService.search(keyword, cafe));
        model.addAttribute("keyword", keyword);
        return "employee/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Cafe cafe = currentUserService.getCurrentCafe();
        Employee employee = new Employee();
        employee.setEmployeeCode(employeeService.generateNextCode(cafe));
        model.addAttribute("employee", employee);
        model.addAttribute("isEdit", false);
        return "employee/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute Employee employee,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "employee/form";
        }
        if (employeeService.existsByCode(employee.getEmployeeCode())) {
            result.rejectValue("employeeCode", "duplicate", "Mã nhân viên đã tồn tại");
            model.addAttribute("isEdit", false);
            return "employee/form";
        }
        Cafe cafe = currentUserService.getCurrentCafe();
        employee.setCafe(cafe);
        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Thêm nhân viên thành công!");
        return "redirect:/employees";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại: " + id));
        model.addAttribute("employee", employee);
        model.addAttribute("isEdit", true);
        return "employee/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Employee employee,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "employee/form";
        }
        Employee existing = employeeService.findById(id).orElseThrow();
        employee.setId(id);
        employee.setCafe(existing.getCafe());
        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Cập nhật nhân viên thành công!");
        return "redirect:/employees";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại: " + id));
        model.addAttribute("employee", employee);
        return "employee/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Xóa nhân viên thành công!");
        return "redirect:/employees";
    }
}
