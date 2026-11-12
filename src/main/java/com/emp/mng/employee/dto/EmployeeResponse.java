package com.emp.mng.employee.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class EmployeeResponse {
    private Long id;

    private String employeeNo;

    private String fullName;

    private String phoneNumber;

    private String email;

    private Long departmentId;

    private String departmentName;

    private String jobTitle;

    private LocalDate employmentDate;

    private String employmentStatus;

    private Long managerId;

    private String managerName;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
