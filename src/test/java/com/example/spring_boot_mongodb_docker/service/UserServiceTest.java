package com.example.spring_boot_mongodb_docker.service;

import com.example.spring_boot_mongodb_docker.model.User;
import com.example.spring_boot_mongodb_docker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
    }

    @Test
    void testFindAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindUserById() {
        // Arrange
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.getUserById("1");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        verify(userRepository, times(1)).findById("1");
    }

    @Test
    void testCreateUser() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User savedUser = userService.createUser(newUser);

        // Assert
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId("1");
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPassword("newpassword");

        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        Optional<User> result = userService.updateUser("1", updatedUser);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("updateduser", result.get().getUsername());
        assertEquals("updated@example.com", result.get().getEmail());
        verify(userRepository, times(1)).findById("1");
        verify(passwordEncoder, times(1)).encode("newpassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        // Arrange
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById("1");

        // Act
        boolean result = userService.deleteUser("1");

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).findById("1");
        verify(userRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteUserNotFound() {
        // Arrange
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // Act
        boolean result = userService.deleteUser("999");

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findById("999");
        verify(userRepository, never()).deleteById(anyString());
    }
}

