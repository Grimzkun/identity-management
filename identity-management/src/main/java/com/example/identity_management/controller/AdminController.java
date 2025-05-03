package com.example.identity_management.controller;

import com.example.identity_management.model.AuditLog;
import com.example.identity_management.model.User;
import com.example.identity_management.security.UserDetailsImpl;
import com.example.identity_management.service.AuditLogService;
import com.example.identity_management.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable("id") Long id, Model model) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/view-user";
        }
        return "redirect:/admin/users?error";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/create-user";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam(required = false) List<String> roles,
                             @RequestParam(defaultValue = "true") boolean enabled,
                             @AuthenticationPrincipal UserDetailsImpl adminDetails,
                             HttpServletRequest request) {

        // Create a new user object
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(enabled);

        // Set default role if none provided
        Set<String> roleSet = new HashSet<>();
        if (roles != null && !roles.isEmpty()) {
            roleSet.addAll(roles);
        } else {
            roleSet.add("user");
        }

        // Get admin's IP address for audit logging
        String ipAddress = request.getRemoteAddr();

        // Register the user with the specified roles
        userService.registerUser(user, roleSet, ipAddress);

        // Log user creation by admin
        auditLogService.logEvent("USER_CREATION", adminDetails.getUsername(),
                "Admin created user: " + user.getUsername(), ipAddress);

        return "redirect:/admin/users?created";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable("id") Long id, Model model) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/edit-user";
        }
        return "redirect:/admin/users?error";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable("id") Long id,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam(required = false) List<String> roles,
                             @RequestParam(required = false) String newPassword,
                             @RequestParam(defaultValue = "false") boolean enabled,
                             @AuthenticationPrincipal UserDetailsImpl adminDetails,
                             HttpServletRequest request) {
        Optional<User> existingUserOpt = userService.getUserById(id);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Update basic user information
            existingUser.setFirstName(firstName);
            existingUser.setLastName(lastName);
            existingUser.setEnabled(enabled);

            // Update password if provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                existingUser.setPassword(newPassword);

                // Log password change
                auditLogService.logEvent("PASSWORD_RESET", adminDetails.getUsername(),
                        "Admin reset password for user: " + existingUser.getUsername(),
                        request.getRemoteAddr());
            }

            // Save updated user information
            userService.updateUser(existingUser);

            // Update roles if provided
            if (roles != null && !roles.isEmpty()) {
                Set<String> roleSet = new HashSet<>(roles);
                userService.updateUserRoles(id, roleSet, adminDetails.getUsername(), request.getRemoteAddr());
            }

            return "redirect:/admin/users?updated";
        }

        return "redirect:/admin/users?error";
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetailsImpl adminDetails,
                             HttpServletRequest request) {
        userService.deleteUser(id, adminDetails.getUsername(), request.getRemoteAddr());
        return "redirect:/admin/users?deleted";
    }

    @GetMapping("/audit-logs")
    public String viewAuditLogs(Model model) {
        List<AuditLog> logs = auditLogService.getAllLogs();
        model.addAttribute("logs", logs);
        return "admin/audit-logs";
    }

    @GetMapping("/audit-logs/user/{username}")
    public String viewUserAuditLogs(@PathVariable("username") String username, Model model) {
        List<AuditLog> logs = auditLogService.getLogsByUsername(username);
        model.addAttribute("logs", logs);
        model.addAttribute("username", username);
        return "admin/user-audit-logs";
    }
}