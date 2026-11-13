package com.emp.mng.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username cannot exceed 100 characters")
    private String username;

    @NotBlank(message = "Initial password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String initialPassword;

    @NotBlank(message = "Role code is required")
    private String roleCode;
}