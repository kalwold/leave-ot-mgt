package com.emp.mng.user.repository;

import com.emp.mng.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmployee_Id(Long employeeId);

    boolean existsByUsername(String username);

    boolean existsByEmployee_Id(Long employeeId);
//    Optional<User> findByRefreshTokenHash(String refreshTokenHash);
//prevent two requests from modifying the same user's refresh-token record at the same time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.refreshTokenHash = :tokenHash")
    Optional<User> findByRefreshTokenHash(
            @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);
}
