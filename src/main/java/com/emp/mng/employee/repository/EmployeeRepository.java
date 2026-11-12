package com.emp.mng.employee.repository;

import com.emp.mng.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeNo(String employeeNo);
    boolean existsByEmployeeNo(String employeeNo);
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Employee> findAllByDepartmentId(Long departmentId);
    List<Employee> findAllByManagerId(Long managerId);
    List<Employee> findAllByEmploymentStatus(String employmentStatus);
}
