package com.emp.mng.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private Long employeeId;
    private String employeeNo;
    private String employeeName;
    private String username;
    private Boolean active;
    private Boolean mustChangePassword;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
    private String roleCode;
    private String roleName;
}
