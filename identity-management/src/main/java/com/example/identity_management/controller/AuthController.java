package com.example.identity_management.controller;

import com.example.identity_management.model.User;
import com.example.identity_management.service.AuditLogService;
import com.example.identity_management.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashSet;
import java.util.Set;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AuthController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               HttpServletRequest request) {
        // Validate user input
        if (result.hasErrors()) {
            return "register";
        }

        // Check if username exists
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username is already taken");
            return "register";
        }

        // Check if email exists
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email is already in use");
            return "register";
        }

        // Set default role as USER
        Set<String> roles = new HashSet<>();
        roles.add("user");

        // Get client IP address
        String ipAddress = request.getRemoteAddr();

        // Register the user
        userService.registerUser(user, roles, ipAddress);

        // Log login attempt success
        auditLogService.logEvent("LOGIN_SUCCESS", user.getUsername(),
                "User logged in successfully after registration", ipAddress);

        return "redirect:/login?registered";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}