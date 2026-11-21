package com.emp.mng.user.repository;

import com.emp.mng.user.entity.UserRole;
import com.emp.mng.user.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("""
            SELECT ur FROM UserRole ur
            JOIN FETCH ur.role
            WHERE ur.user.id = :userId
            """)
    List<UserRole> findAllByUser_Id(@Param("userId") Long userId);
    boolean existsByUser_IdAndRole_Id(Long userId, Long roleId);

    void deleteAllByUser_Id(Long userId);
}


