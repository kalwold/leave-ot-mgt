package com.emp.mng.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 50)
    private String code;

    private String description;
}