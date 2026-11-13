package com.emp.mng.user.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles", schema = "leave_overtime")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 30)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;
}
