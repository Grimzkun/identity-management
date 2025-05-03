package com.example.identity_management.config;

import com.example.identity_management.model.Role;
import com.example.identity_management.model.User;
import com.example.identity_management.repository.RoleRepository;
import com.example.identity_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DbInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DbInitializer(RoleRepository roleRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Initialize roles if they don't exist
        initRoles();

        // Create admin user if it doesn't exist
        createAdminIfNotFound();
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            // Create USER role
            Role userRole = new Role();
            userRole.setName(Role.ERole.ROLE_USER);
            roleRepository.save(userRole);

            // Create ADMIN role
            Role adminRole = new Role();
            adminRole.setName(Role.ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);

            System.out.println("Roles initialized successfully!");
        }
    }

    private void createAdminIfNotFound() {
        if (!userRepository.existsByUsername("admin")) {
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Default password
            admin.setEmail("admin@example.com");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            // Assign ADMIN role
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Admin Role not found."));
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);

            System.out.println("Admin user created successfully!");
        }
    }
}