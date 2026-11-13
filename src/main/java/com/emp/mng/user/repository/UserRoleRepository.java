package com.emp.mng.user.repository;

import com.emp.mng.user.entity.UserRole;
import com.emp.mng.user.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
List<UserRole> findAllByUser_Id(Long userId);
    boolean existsByUser_IdAndRole_Id(Long userId, Long roleId);

    void deleteAllByUser_Id(Long userId);
}


