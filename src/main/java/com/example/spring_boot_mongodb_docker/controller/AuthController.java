package com.example.spring_boot_mongodb_docker.controller;

import com.example.spring_boot_mongodb_docker.model.Role;
import com.example.spring_boot_mongodb_docker.model.User;
import com.example.spring_boot_mongodb_docker.service.JwtService;
import com.example.spring_boot_mongodb_docker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.spring_boot_mongodb_docker.config.MetricsConfig;



import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;
    private final MetricsConfig metricsConfig;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          UserService userService,
                          MetricsConfig metricsConfig) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.metricsConfig = metricsConfig;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        // Check if username already exists
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username already exists"));
        }

        // Validate password complexity
        if (!isPasswordValid(user.getPassword())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                            "one lowercase letter, one number, and one special character")
            );
        }

        // Save user with default USER role
        User savedUser = userService.save(user);

        // Generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String jwt = jwtService.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", savedUser.getUsername());
        response.put("roles", savedUser.getRoles());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Inside the login method in AuthController.java
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username and password are required"));
        }

        try {
            // Record authentication time
            return metricsConfig.getAuthenticationTimer().record(() -> {
                try {
                    // First check if the user exists and is not locked
                    Optional<User> userOpt = userService.findByUsername(username);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        if (!user.isAccountNonLocked()) {
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                    .body(Map.of("message", "Account is locked. Please contact an administrator."));
                        }

                        if (!user.isEnabled()) {
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                    .body(Map.of("message", "Account is disabled. Please contact an administrator."));
                        }
                    }

                    // Proceed with authentication
                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(username, password)
                    );

                    // Increment success counter
                    metricsConfig.getLoginSuccessCounter().increment();

                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    String jwt = jwtService.generateToken(userDetails);

                    Map<String, Object> response = new HashMap<>();
                    response.put("token", jwt);
                    response.put("username", username);

                    if (userOpt.isPresent()) {
                        response.put("roles", userOpt.get().getRoles());
                    }

                    return ResponseEntity.ok(response);
                } catch (BadCredentialsException e) {
                    // Increment failure counter
                    metricsConfig.getLoginFailureCounter().increment();
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "Invalid username or password"));
                } catch (LockedException e) {
                    metricsConfig.getLoginFailureCounter().increment();
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "Account is locked. Please contact an administrator."));
                } catch (DisabledException e) {
                    metricsConfig.getLoginFailureCounter().increment();
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "Account is disabled. Please contact an administrator."));
                } catch (Exception e) {
                    metricsConfig.getLoginFailureCounter().increment();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("message", "An error occurred during authentication: " + e.getMessage()));
                }
            });
        } catch (Exception e) {
            metricsConfig.getLoginFailureCounter().increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }


    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdminUser(@Valid @RequestBody User user) {
        // This endpoint should only be used during initial setup or by existing admins
        // In a production environment, you might want to secure this further

        // Check if username already exists
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username already exists"));
        }

        // Validate password complexity
        if (!isPasswordValid(user.getPassword())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                            "one lowercase letter, one number, and one special character")
            );
        }

        // Add ADMIN role
        user.addRole(Role.ROLE_ADMIN);

        // Save user
        User savedUser = userService.save(user);
        savedUser.setPassword(null); // Remove password from response

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // Helper method to validate password complexity
    private boolean isPasswordValid(String password) {
        // Password must be at least 8 characters long and contain at least one uppercase letter,
        // one lowercase letter, one number, and one special character
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(passwordRegex);
    }
}
