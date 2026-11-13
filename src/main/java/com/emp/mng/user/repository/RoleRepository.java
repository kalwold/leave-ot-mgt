package com.emp.mng.user.repository;

import com.emp.mng.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);
}
