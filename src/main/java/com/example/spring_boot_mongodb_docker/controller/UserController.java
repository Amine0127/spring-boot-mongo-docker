package com.example.spring_boot_mongodb_docker.controller;

import com.example.spring_boot_mongodb_docker.model.User;
import com.example.spring_boot_mongodb_docker.service.JwtService;
import com.example.spring_boot_mongodb_docker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // Get current user profile
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            User userObj = user.get();
            // Don't return the password in the response
            userObj.setPassword(null);
            return ResponseEntity.ok(userObj);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }

    // Get all users (admin only) with pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<User> pageUsers = userService.findAllPaginated(pageable);

        // Remove passwords from response
        List<User> users = pageUsers.getContent();
        users.forEach(user -> user.setPassword(null));

        Map<String, Object> response = new HashMap<>();
        response.put("content", users);
        response.put("currentPage", pageUsers.getNumber());
        response.put("totalItems", pageUsers.getTotalElements());
        response.put("totalPages", pageUsers.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // Delete current user
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        boolean deleted = userService.deleteByUsername(username);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update password
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> passwordData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Current password and new password are required");
        }

        // Validate password complexity
        if (!isPasswordValid(newPassword)) {
            return ResponseEntity.badRequest().body(
                    "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                            "one lowercase letter, one number, and one special character"
            );
        }

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Verify current password
            if (!userService.isPasswordValid(currentPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current password is incorrect");
            }

            // Update password
            User updatedUser = userService.updatePassword(username, newPassword);
            updatedUser.setPassword(null); // Remove password from response
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper method to validate password complexity
    private boolean isPasswordValid(String password) {
        // Password must be at least 8 characters long and contain at least one uppercase letter,
        // one lowercase letter, one number, and one special character
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(passwordRegex);
    }
}
