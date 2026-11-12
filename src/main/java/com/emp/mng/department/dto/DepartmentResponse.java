package com.emp.mng.department.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DepartmentResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
}
