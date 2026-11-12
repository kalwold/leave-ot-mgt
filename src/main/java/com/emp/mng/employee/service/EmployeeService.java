package com.emp.mng.employee.service;


import com.emp.mng.department.entity.Department;
import com.emp.mng.department.repository.DepartmentRepository;
import com.emp.mng.employee.dto.EmployeeRequest;
import com.emp.mng.employee.dto.EmployeeResponse;
import com.emp.mng.employee.entity.Employee;
import com.emp.mng.employee.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    // Register employee
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        String employeeNo = request.getEmployeeNo().trim().toUpperCase();

        if (employeeRepository.existsByEmployeeNo(employeeNo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee number already exists"
            );
        }

        String email = normalizeEmail(request.getEmail());

        if (email != null && employeeRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email address already exists"
            );
        }

        Department department = getDepartment(request.getDepartmentId());

        Employee manager = getManager(
                request.getManagerId(),
                department,
                null
        );

        Employee employee = new Employee();

        employee.setEmployeeNo(employeeNo);
        employee.setFullName(request.getFullName().trim());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setJobTitle(request.getJobTitle());
        employee.setEmploymentDate(request.getEmploymentDate());
        employee.setEmploymentStatus(
                request.getEmploymentStatus().trim().toUpperCase()
        );
        employee.setManager(manager);

        return mapToResponse(employeeRepository.save(employee));
    }

    // Update employee
    public EmployeeResponse updateEmployee(
            Long id,
            EmployeeRequest request
    ) {

        Employee employee = getEmployeeEntity(id);

        String employeeNo = request.getEmployeeNo().trim().toUpperCase();

        employeeRepository.findByEmployeeNo(employeeNo)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Employee number already exists"
                    );
                });

        String email = normalizeEmail(request.getEmail());

        if (email != null) {
            employeeRepository.findByEmail(email)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Email address already exists"
                        );
                    });
        }

        Department department = getDepartment(request.getDepartmentId());

        Employee manager = getManager(
                request.getManagerId(),
                department,
                id
        );

        employee.setEmployeeNo(employeeNo);
        employee.setFullName(request.getFullName().trim());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setJobTitle(request.getJobTitle());
        employee.setEmploymentDate(request.getEmploymentDate());
        employee.setEmploymentStatus(
                request.getEmploymentStatus().trim().toUpperCase()
        );
        employee.setManager(manager);

        return mapToResponse(employeeRepository.save(employee));
    }

    // Get employee by ID
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return mapToResponse(getEmployeeEntity(id));
    }

    // Get all employees
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get employees by department
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartment(Long departmentId) {

        if (!departmentRepository.existsById(departmentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Department not found"
            );
        }

        return employeeRepository.findAllByDepartmentId(departmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get employees by manager
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByManager(Long managerId) {

        getEmployeeEntity(managerId);

        return employeeRepository.findAllByManagerId(managerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get employees by employment status
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByStatus(String status) {

        return employeeRepository
                .findAllByEmploymentStatus(status.trim().toUpperCase())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Validate and retrieve department
    private Department getDepartment(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found with ID: " + departmentId
                ));

        if (!Boolean.TRUE.equals(department.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign an employee to an inactive department"
            );
        }

        return department;
    }

    // Validate and retrieve manager
    private Employee getManager(
            Long managerId,
            Department department,
            Long employeeId
    ) {

        if (managerId == null) {
            return null;
        }

        Employee manager = getEmployeeEntity(managerId);

        if (employeeId != null && manager.getId().equals(employeeId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "An employee cannot be their own manager"
            );
        }

        if (!manager.getDepartment().getId().equals(department.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Manager must belong to the same department"
            );
        }

        return manager;
    }

    // Retrieve employee entity
    private Employee getEmployeeEntity(Long id) {

        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found with ID: " + id
                ));
    }

    // Map entity to response DTO
    private EmployeeResponse mapToResponse(Employee employee) {

        EmployeeResponse response = new EmployeeResponse();

        response.setId(employee.getId());
        response.setEmployeeNo(employee.getEmployeeNo());
        response.setFullName(employee.getFullName());
        response.setPhoneNumber(employee.getPhoneNumber());
        response.setEmail(employee.getEmail());

        response.setDepartmentId(employee.getDepartment().getId());
        response.setDepartmentName(employee.getDepartment().getName());

        response.setJobTitle(employee.getJobTitle());
        response.setEmploymentDate(employee.getEmploymentDate());
        response.setEmploymentStatus(employee.getEmploymentStatus());

        if (employee.getManager() != null) {
            response.setManagerId(employee.getManager().getId());
            response.setManagerName(employee.getManager().getFullName());
        }

        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());

        return response;
    }


    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }
}