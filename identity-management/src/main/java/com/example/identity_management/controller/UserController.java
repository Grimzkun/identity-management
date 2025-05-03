package com.example.identity_management.controller;

import com.example.identity_management.model.User;
import com.example.identity_management.security.UserDetailsImpl;
import com.example.identity_management.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        System.out.println("Dashboard accessed by: " + userDetails.getUsername());
        Optional<User> user = userService.getUserByUsername(userDetails.getUsername());
        if (user.isPresent()) {
            System.out.println("User found in database");
        } else {
            System.out.println("User not found in database");
        }
        model.addAttribute("user", user.orElse(new User()));
        return "dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Optional<User> user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user.orElse(new User()));
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Optional<User> user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user.orElse(new User()));
        return "edit-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @Valid @ModelAttribute("user") User updatedUser,
                                BindingResult result,
                                HttpServletRequest request) {
        if (result.hasErrors()) {
            return "edit-profile";
        }

        Optional<User> existingUserOpt = userService.getUserByUsername(userDetails.getUsername());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Update only allowed fields (not username/password)
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());

            // Check if email is changed and not already taken
            if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
                if (userService.existsByEmail(updatedUser.getEmail())) {
                    result.rejectValue("email", "error.user", "Email is already in use");
                    return "edit-profile";
                }
                existingUser.setEmail(updatedUser.getEmail());
            }

            userService.updateUser(existingUser);
            return "redirect:/profile?updated";
        }

        return "redirect:/profile?error";
    }
}