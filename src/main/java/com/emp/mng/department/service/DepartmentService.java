package com.emp.mng.department.service;

import com.emp.mng.department.dto.DepartmentRequest;
import com.emp.mng.department.dto.DepartmentResponse;
import com.emp.mng.department.entity.Department;
import com.emp.mng.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // Create department
    public DepartmentResponse createDepartment(DepartmentRequest request) {

        String name = request.getName().trim();
        String code = normalizeCode(request.getCode());

        // Check duplicate department name
        if (departmentRepository.existsByName(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department name already exists"
            );
        }

        // Check duplicate department code
        if (code != null && departmentRepository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department code already exists"
            );
        }

        Department department = new Department();

        department.setName(name);
        department.setCode(code);
        department.setDescription(request.getDescription());
        department.setActive(true);

        Department saved = departmentRepository.save(department);

        return mapToResponse(saved);
    }

    // Update department
    public DepartmentResponse updateDepartment(
            Long id,
            DepartmentRequest request) {

        Department department = getDepartmentEntity(id);

        String name = request.getName().trim();
        String code = normalizeCode(request.getCode());

        // Check duplicate name excluding current department
        if (departmentRepository.existsByName(name)
                && !department.getName().equals(name)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department name already exists"
            );
        }

        // Check duplicate code excluding current department
        if (code != null
                && departmentRepository.existsByCode(code)
                && !code.equals(department.getCode())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department code already exists"
            );
        }

        department.setName(name);
        department.setCode(code);
        department.setDescription(request.getDescription());

        Department updated = departmentRepository.save(department);

        return mapToResponse(updated);
    }

    // Get department by ID
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {

        Department department = getDepartmentEntity(id);

        return mapToResponse(department);
    }

    // Get all departments
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {

        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get active departments
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getActiveDepartments() {

        return departmentRepository.findAllByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Activate or deactivate department
    public DepartmentResponse changeDepartmentStatus(
            Long id,
            boolean active) {

        Department department = getDepartmentEntity(id);

        department.setActive(active);

        Department updated = departmentRepository.save(department);

        return mapToResponse(updated);
    }

    // Find department entity
    private Department getDepartmentEntity(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found with ID: " + id
                ));
    }

    // Convert entity to response DTO
    private DepartmentResponse mapToResponse(Department department) {

        DepartmentResponse response = new DepartmentResponse();

        response.setId(department.getId());
        response.setName(department.getName());
        response.setCode(department.getCode());
        response.setDescription(department.getDescription());
        response.setActive(department.getActive());
        response.setCreatedAt(department.getCreatedAt());

        return response;
    }

    // Normalize optional department code
    private String normalizeCode(String code) {

        if (code == null || code.isBlank()) {
            return null;
        }

        return code.trim().toUpperCase();
    }
}