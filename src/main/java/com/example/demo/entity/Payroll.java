package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payrolls",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "month", "year"}))
@Data
@NoArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"attendances", "user", "cafe"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    /** Lương cơ bản (copy từ nhân viên tại thời điểm tính) */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    /** Số ngày công chuẩn trong tháng */
    @Column(nullable = false)
    private Integer standardWorkDays = 26;

    /** Số ngày công thực tế (tự động tính từ chấm công) */
    @Column(nullable = false)
    private Integer actualWorkDays = 0;

    /** Số ngày nghỉ phép có lương */
    @Column(nullable = false)
    private Integer paidLeaveDays = 0;

    /** Giờ làm thêm */
    @Column(precision = 8, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    /** Hệ số lương làm thêm (mặc định 1.5x) */
    @Column(precision = 5, scale = 2)
    private BigDecimal overtimeRate = new BigDecimal("1.5");

    /** Phụ cấp (ăn trưa, xăng xe, điện thoại...) */
    @Column(precision = 15, scale = 2)
    private BigDecimal allowance = BigDecimal.ZERO;

    /** Khấu trừ (bảo hiểm, vi phạm...) */
    @Column(precision = 15, scale = 2)
    private BigDecimal deduction = BigDecimal.ZERO;

    /** Lương thực nhận = (baseSalary/standardWorkDays)*(actualWorkDays+paidLeaveDays)
     *                   + overtimeHours * (baseSalary/standardWorkDays/8) * overtimeRate
     *                   + allowance - deduction */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary = BigDecimal.ZERO;

    /** PENDING=chờ duyệt, APPROVED=đã duyệt, PAID=đã thanh toán */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 500)
    private String note;

    private LocalDate paidDate;

    @Column(updatable = false)
    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    /** Tính lại lương thực nhận */
    public void recalculate() {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) == 0
                || standardWorkDays == null || standardWorkDays == 0) {
            netSalary = BigDecimal.ZERO;
            return;
        }
        BigDecimal dailyRate = baseSalary.divide(BigDecimal.valueOf(standardWorkDays), 4, java.math.RoundingMode.HALF_UP);
        int effectiveDays = (actualWorkDays == null ? 0 : actualWorkDays)
                          + (paidLeaveDays == null ? 0 : paidLeaveDays);
        BigDecimal workSalary = dailyRate.multiply(BigDecimal.valueOf(effectiveDays));

        BigDecimal overtimePay = BigDecimal.ZERO;
        if (overtimeHours != null && overtimeHours.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal hourlyRate = dailyRate.divide(BigDecimal.valueOf(8), 4, java.math.RoundingMode.HALF_UP);
            BigDecimal rate = (overtimeRate != null) ? overtimeRate : new BigDecimal("1.5");
            overtimePay = hourlyRate.multiply(rate).multiply(overtimeHours);
        }

        BigDecimal all = (allowance != null) ? allowance : BigDecimal.ZERO;
        BigDecimal ded = (deduction != null) ? deduction : BigDecimal.ZERO;

        netSalary = workSalary.add(overtimePay).add(all).subtract(ded)
                              .setScale(0, java.math.RoundingMode.HALF_UP);
        if (netSalary.compareTo(BigDecimal.ZERO) < 0) netSalary = BigDecimal.ZERO;
    }
}
