package com.agroya.controller;

import com.agroya.dto.request.SignupRequest;
import com.agroya.dto.response.MessageResponse;
import com.agroya.model.Role;
import com.agroya.model.User;
import com.agroya.repository.RoleRepository;
import com.agroya.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthController authController;

    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@agroya.com");
        signupRequest.setPassword("password123");
        signupRequest.setNombre("Test");
        signupRequest.setApellido("User");
        signupRequest.setRole(Set.of("comprador"));
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        
        Role role = new Role();
        role.setName("ROLE_COMPRADOR");
        when(roleRepository.findByName("ROLE_COMPRADOR")).thenReturn(Optional.of(role));

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof MessageResponse);
        assertEquals("User registered successfully!", ((MessageResponse) response.getBody()).getMessage());
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Error: Email is already in use!", ((MessageResponse) response.getBody()).getMessage());
        
        verify(userRepository, never()).save(any(User.class));
    }
}
