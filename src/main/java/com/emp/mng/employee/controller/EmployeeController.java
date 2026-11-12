package com.emp.mng.employee.controller;


import com.emp.mng.employee.dto.EmployeeRequest;
import com.emp.mng.employee.dto.EmployeeResponse;
import com.emp.mng.employee.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // Register employee
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(request));
    }

    // Update employee
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        return ResponseEntity.ok(
                employeeService.updateEmployee(id, request)
        );
    }

    // Get employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                employeeService.getEmployeeById(id)
        );
    }

    // Get all employees
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(
                employeeService.getAllEmployees()
        );
    }

    // Get employees by department
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByDepartment(
            @PathVariable Long departmentId
    ) {
        return ResponseEntity.ok(
                employeeService.getEmployeesByDepartment(departmentId)
        );
    }

    // Get employees by manager
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByManager(
            @PathVariable Long managerId
    ) {
        return ResponseEntity.ok(
                employeeService.getEmployeesByManager(managerId)
        );
    }

    // Get employees by employment status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                employeeService.getEmployeesByStatus(status)
        );
    }
}

