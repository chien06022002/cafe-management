package com.example.demo.repository;

import com.example.demo.entity.Cafe;
import com.example.demo.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    List<Employee> findByStatus(String status);
    List<Employee> findByFullNameContainingIgnoreCaseOrEmployeeCodeContainingIgnoreCase(String name, String code);
    boolean existsByEmployeeCode(String employeeCode);

    // --- Cafe-scoped ---
    List<Employee> findByCafe(Cafe cafe);
    List<Employee> findByCafeAndStatus(Cafe cafe, String status);

    @Query("SELECT e FROM Employee e WHERE e.cafe = :cafe AND " +
           "(LOWER(e.fullName) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    List<Employee> searchByCafe(@Param("cafe") Cafe cafe, @Param("keyword") String keyword);

    long countByCafe(Cafe cafe);
    long countByCafeAndStatus(Cafe cafe, String status);
}
