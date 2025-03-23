package com.example.spring_boot_mongodb_docker.service;

import com.example.spring_boot_mongodb_docker.config.MetricsConfig;
import com.example.spring_boot_mongodb_docker.model.Role;
import com.example.spring_boot_mongodb_docker.model.User;
import com.example.spring_boot_mongodb_docker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MetricsConfig metricsConfig;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MetricsConfig metricsConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.metricsConfig = metricsConfig;
    }

    public User save(User user) {
        logger.info("Saving new user: {}", user.getUsername());

        // Increment user registration counter
        metricsConfig.getUserRegistrationCounter().increment();

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default role if not set
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.addRole(Role.ROLE_USER);
        }

        // Record database operation time
        return metricsConfig.recordDatabaseOperationTime("saveUser", () ->
                userRepository.save(user)
        );
    }

    public User saveWithoutEncodingPassword(User user) {
        logger.info("Saving user without encoding password: {}", user.getUsername());
        return metricsConfig.recordDatabaseOperationTime("saveUserWithoutEncodingPassword", () ->
                userRepository.save(user)
        );
    }

    public List<User> findAll() {
        logger.debug("Finding all users");
        return metricsConfig.recordDatabaseOperationTime("findAllUsers", () ->
                userRepository.findAll()
        );
    }

    public Page<User> findAllPaginated(Pageable pageable) {
        logger.debug("Finding paginated users with pageable: {}", pageable);
        return metricsConfig.recordDatabaseOperationTime("findAllUsersPaginated", () ->
                userRepository.findAll(pageable)
        );
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        return metricsConfig.recordDatabaseOperationTime("findUserByUsername", () ->
                userRepository.findByUsername(username)
        );
    }

    public User updateRoles(String username, Set<Role> roles) {
        logger.info("Updating roles for user: {}", username);
        return metricsConfig.recordDatabaseOperationTime("updateUserRoles", () -> {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRoles(roles);
                return userRepository.save(user);
            } else {
                throw new RuntimeException("User not found: " + username);
            }
        });
    }

    public boolean deleteByUsername(String username) {
        logger.info("Deleting user: {}", username);
        return metricsConfig.recordDatabaseOperationTime("deleteUserByUsername", () -> {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                userRepository.delete(userOpt.get());
                return true;
            }
            return false;
        });
    }

    public User updatePassword(String username, String newPassword) {
        logger.info("Updating password for user: {}", username);
        return metricsConfig.recordDatabaseOperationTime("updateUserPassword", () -> {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(passwordEncoder.encode(newPassword));
                return userRepository.save(user);
            } else {
                throw new RuntimeException("User not found: " + username);
            }
        });
    }

    public boolean isPasswordValid(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // Add to UserService.java
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
