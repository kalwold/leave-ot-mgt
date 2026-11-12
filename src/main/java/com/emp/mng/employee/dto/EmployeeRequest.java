package com.emp.mng.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "Employee number is required")
    @Size(max = 50)
    private String employeeNo;

    @NotBlank(message = "Full name is required")
    @Size(max = 150)
    private String fullName;

    @Size(max = 30)
    private String phoneNumber;

    @Email(message = "Invalid email address")
    @Size(max = 150)
    private String email;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @Size(max = 150)
    private String jobTitle;

    @NotNull(message = "Employment date is required")
    private LocalDate employmentDate;

    @NotBlank(message = "Employment status is required")
    private String employmentStatus;

    private Long managerId;
}
