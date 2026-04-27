package com.example.demo.repository;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByMonthAndYear(int month, int year);
    List<Payroll> findByEmployee(Employee employee);
    Optional<Payroll> findByEmployeeAndMonthAndYear(Employee employee, int month, int year);
    List<Payroll> findByMonthAndYearAndStatus(int month, int year, String status);

    @Query("SELECT p FROM Payroll p JOIN FETCH p.employee WHERE p.month = :month AND p.year = :year ORDER BY p.employee.fullName")
    List<Payroll> findByMonthAndYearWithEmployee(@Param("month") int month, @Param("year") int year);

    // --- Cafe-scoped ---
    @Query("SELECT p FROM Payroll p JOIN FETCH p.employee WHERE p.employee.cafe = :cafe AND p.month = :month AND p.year = :year ORDER BY p.employee.fullName")
    List<Payroll> findByCafeAndMonthAndYearWithEmployee(@Param("cafe") Cafe cafe, @Param("month") int month, @Param("year") int year);

    @Query("SELECT p FROM Payroll p WHERE p.employee.cafe = :cafe AND p.month = :month AND p.year = :year AND p.status = :status")
    List<Payroll> findByCafeAndMonthAndYearAndStatus(@Param("cafe") Cafe cafe, @Param("month") int month, @Param("year") int year, @Param("status") String status);
}
