package com.example.demo.repository;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByDate(LocalDate date);
    List<ShiftAssignment> findByEmployee(Employee employee);
    List<ShiftAssignment> findByDateBetween(LocalDate start, LocalDate end);
    List<ShiftAssignment> findByEmployeeAndDateBetween(Employee employee, LocalDate start, LocalDate end);
    List<ShiftAssignment> findByShiftAndDate(Shift shift, LocalDate date);
    Optional<ShiftAssignment> findByEmployeeAndDate(Employee employee, LocalDate date);

    @Query("SELECT sa FROM ShiftAssignment sa JOIN FETCH sa.employee JOIN FETCH sa.shift " +
           "WHERE sa.employee = :employee AND sa.date BETWEEN :start AND :end ORDER BY sa.date, sa.shift.startTime")
    List<ShiftAssignment> findByEmployeeAndDateBetweenWithDetails(@Param("employee") Employee employee,
                                                                  @Param("start") LocalDate start,
                                                                  @Param("end") LocalDate end);

    @Query("SELECT sa FROM ShiftAssignment sa JOIN FETCH sa.employee JOIN FETCH sa.shift " +
           "WHERE sa.date BETWEEN :start AND :end ORDER BY sa.date, sa.shift.startTime")
    List<ShiftAssignment> findByDateRangeWithDetails(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // --- Cafe-scoped ---
    @Query("SELECT sa FROM ShiftAssignment sa JOIN FETCH sa.employee JOIN FETCH sa.shift " +
           "WHERE sa.employee.cafe = :cafe AND sa.date BETWEEN :start AND :end ORDER BY sa.date, sa.shift.startTime")
    List<ShiftAssignment> findByCafeAndDateRangeWithDetails(@Param("cafe") Cafe cafe,
                                                             @Param("start") LocalDate start,
                                                             @Param("end") LocalDate end);
}
