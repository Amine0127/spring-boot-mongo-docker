package com.example.spring_boot_mongodb_docker.controller;

import com.example.spring_boot_mongodb_docker.model.Role;
import com.example.spring_boot_mongodb_docker.model.User;
import com.example.spring_boot_mongodb_docker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
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

    @PutMapping("/users/{username}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable String username,
                                             @RequestBody Map<String, List<String>> rolesMap) {
        try {
            List<String> roleStrings = rolesMap.get("roles");
            Set<Role> roles = roleStrings.stream()
                    .map(role -> {
                        try {
                            return Role.valueOf(role);
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid role: " + role);
                        }
                    })
                    .collect(Collectors.toSet());

            User updatedUser = userService.updateRoles(username, roles);
            updatedUser.setPassword(null); // Remove password from response
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        boolean deleted = userService.deleteByUsername(username);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/users/{username}/lock")
    public ResponseEntity<?> lockUserAccount(@PathVariable String username) {
        try {
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Don't modify the password
                user.setAccountNonLocked(false);
                User updatedUser = userService.saveWithoutEncodingPassword(user);
                updatedUser.setPassword(null); // Remove password from response
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/users/{username}/unlock")
    public ResponseEntity<?> unlockUserAccount(@PathVariable String username) {
        try {
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Don't modify the password
                user.setAccountNonLocked(true);
                User updatedUser = userService.saveWithoutEncodingPassword(user);
                updatedUser.setPassword(null); // Remove password from response
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
