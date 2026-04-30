package com.example.demo.repository;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee WHERE a.date = :date ORDER BY a.employee.fullName")
    List<Attendance> findByDate(@Param("date") LocalDate date);

    List<Attendance> findByEmployee(Employee employee);
    List<Attendance> findByEmployeeAndDateBetween(Employee employee, LocalDate start, LocalDate end);
    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND a.status = 'PRESENT'")
    long countPresentByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND a.status = 'ABSENT'")
    long countAbsentByDate(@Param("date") LocalDate date);

    // --- Cafe-scoped ---
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE e.cafe = :cafe AND a.date = :date ORDER BY e.fullName")
    List<Attendance> findByCafeAndDate(@Param("cafe") Cafe cafe, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE e.cafe = :cafe AND a.date BETWEEN :start AND :end ORDER BY a.date, e.fullName")
    List<Attendance> findByCafeAndDateBetween(@Param("cafe") Cafe cafe, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.cafe = :cafe AND a.date = :date AND a.status = 'PRESENT'")
    long countPresentByCafeAndDate(@Param("cafe") Cafe cafe, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.cafe = :cafe AND a.date = :date AND a.status = 'ABSENT'")
    long countAbsentByCafeAndDate(@Param("cafe") Cafe cafe, @Param("date") LocalDate date);
}
