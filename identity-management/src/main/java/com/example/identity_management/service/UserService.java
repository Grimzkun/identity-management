package com.example.identity_management.service;

import com.example.identity_management.model.Role;
import com.example.identity_management.model.User;
import com.example.identity_management.repository.RoleRepository;
import com.example.identity_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder encoder,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.auditLogService = auditLogService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User registerUser(User user, Set<String> strRoles, String ipAddress) {
        // Encode the password
        user.setPassword(encoder.encode(user.getPassword()));

        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                if ("admin".equals(role)) {
                    Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Log the registration
        auditLogService.logEvent("REGISTRATION", user.getUsername(),
                "User registration successful", ipAddress);

        return savedUser;
    }

    @Transactional
    public User updateUser(User user) {
        // If the password is not encoded (doesn't start with $2a$), encode it
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(encoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id, String performedBy, String ipAddress) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);

            // Log the deletion
            auditLogService.logEvent("USER_DELETION", performedBy,
                    "User deleted: " + user.get().getUsername(), ipAddress);
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void updateUserRoles(Long userId, Set<String> strRoles, String performedBy, String ipAddress) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null || strRoles.isEmpty()) {
                Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    if ("admin".equals(role)) {
                        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                    } else {
                        Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                    }
                });
            }

            // Create a string representation of old roles for logging
            String oldRoles = user.getRoles().stream()
                    .map(r -> r.getName().name())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            // Create a string representation of new roles for logging
            String newRoles = roles.stream()
                    .map(r -> r.getName().name())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            user.setRoles(roles);
            userRepository.save(user);

            // Log the role update
            auditLogService.logEvent("ROLE_UPDATE", performedBy,
                    "Updated roles for user " + user.getUsername() +
                            ". Old roles: [" + oldRoles + "], New roles: [" + newRoles + "]",
                    ipAddress);
        }
    }
}