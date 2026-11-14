package com.emp.mng.user.service;

import com.emp.mng.employee.entity.Employee;
import com.emp.mng.employee.repository.EmployeeRepository;
import com.emp.mng.user.dto.ChangePasswordRequest;
import com.emp.mng.user.dto.UserRequest;
import com.emp.mng.user.dto.UserResponse;
import com.emp.mng.user.entity.Role;
import com.emp.mng.user.entity.User;
import com.emp.mng.user.entity.UserRole;
import com.emp.mng.user.entity.UserRoleId;
import com.emp.mng.user.repository.RoleRepository;
import com.emp.mng.user.repository.UserRepository;
import com.emp.mng.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserRequest request) {

        // Normalize input
        String username = request.getUsername().trim().toLowerCase();
        String roleCode = request.getRoleCode().trim().toUpperCase();

        // Check employee
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found"
                ));

        // Check whether employee already has an account
        if (userRepository.existsByEmployee_Id(employee.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This employee already has a user account"
            );
        }

        // Check username
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        // Find role
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Role not found: " + roleCode
                ));

        // Create user
        User user = new User();

        user.setEmployee(employee);
        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(request.getInitialPassword())
        );
        user.setActive(true);
        user.setMustChangePassword(true);

        User savedUser = userRepository.save(user);

        // Assign role
        UserRole userRole = new UserRole();

        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserId(savedUser.getId());
        userRoleId.setRoleId(role.getId());

        userRole.setId(userRoleId);
        userRole.setUser(savedUser);
        userRole.setRole(role);

        userRoleRepository.save(userRole);

        // Return response
        return mapToResponse(savedUser, role);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .flatMap(user ->
                        userRoleRepository.findAllByUser_Id(user.getId())
                                .stream()
                                .map(userRole -> mapToResponse(
                                        user,
                                        userRole.getRole()
                                ))
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        return userRoleRepository.findAllByUser_Id(user.getId())
                .stream()
                .findFirst()
                .map(userRole -> mapToResponse(user, userRole.getRole()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No role assigned to this user"
                ));
    }


    @Transactional
    public UserResponse updateUserStatus(Long id, Boolean active) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        user.setActive(active);

        User savedUser = userRepository.save(user);

        return userRoleRepository.findAllByUser_Id(savedUser.getId())
                .stream()
                .findFirst()
                .map(userRole -> mapToResponse(savedUser, userRole.getRole()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No role assigned to this user"
                ));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

            if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current Password is incorrect");
            }
            if(!request.getNewPassword().equals(request.getConfirmPassword())){
                throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password Doesn't match");
            }

            user.setPasswordHash(
                    passwordEncoder.encode(request.getNewPassword()));

            user.setMustChangePassword(false);
            userRepository.save(user);

    }

    private UserResponse mapToResponse(User user, Role role) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setEmployeeId(user.getEmployee().getId());
        response.setEmployeeNo(user.getEmployee().getEmployeeNo());
        response.setEmployeeName(user.getEmployee().getFullName());
        response.setUsername(user.getUsername());
        response.setActive(user.getActive());
        response.setMustChangePassword(user.getMustChangePassword());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        response.setRoleCode(role.getRoleCode());
        response.setRoleName(role.getRoleName());

        return response;
    }


}
