package com.portiq.controller;

import com.portiq.dto.AuthResponse;
import com.portiq.dto.LoginRequest;
import com.portiq.model.User;
import com.portiq.security.JwtService;
import com.portiq.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login operations")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with username and password")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request.getUsername(), request.getPassword())
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(new AuthResponse(
                        jwtService.generateToken(user.getUsername(), user.getRole().name()),
                        user.getUsername(),
                        user.getRole().name())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid username or password")));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current logged in user")
    public ResponseEntity<?> me(Authentication authentication) {
        User user = userService.getByUsername(authentication.getName());
        Map<String, Object> body = new HashMap<>();
        body.put("username", user.getUsername());
        body.put("name", user.getName());
        body.put("email", user.getEmail());
        body.put("role", user.getRole().name());
        body.put("managerUsername", user.getManagedBy() != null ? user.getManagedBy().getUsername() : null);
        return ResponseEntity.ok(body);
    }
}