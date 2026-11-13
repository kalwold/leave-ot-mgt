package com.emp.mng.user.repository;

import com.emp.mng.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmployee_Id(Long employeeId);

    boolean existsByUsername(String username);

    boolean existsByEmployee_Id(Long employeeId);
}
