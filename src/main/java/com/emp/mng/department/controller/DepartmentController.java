package com.emp.mng.department.controller;

import com.emp.mng.department.dto.DepartmentRequest;
import com.emp.mng.department.dto.DepartmentResponse;
import com.emp.mng.department.service.DepartmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // Create department
    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(request));
    }

    // Update department
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {

        return ResponseEntity.ok(
                departmentService.updateDepartment(id, request)
        );
    }

    // Get department by ID
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                departmentService.getDepartmentById(id)
        );
    }

    // Get all departments
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {

        return ResponseEntity.ok(
                departmentService.getAllDepartments()
        );
    }

    // Get active departments
    @GetMapping("/active")
    public ResponseEntity<List<DepartmentResponse>> getActiveDepartments() {

        return ResponseEntity.ok(
                departmentService.getActiveDepartments()
        );
    }

    // Activate or deactivate department
    @PatchMapping("/{id}/status")
    public ResponseEntity<DepartmentResponse> changeDepartmentStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        return ResponseEntity.ok(
                departmentService.changeDepartmentStatus(id, active)
        );
    }
}
