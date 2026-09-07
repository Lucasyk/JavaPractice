package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.example.demo.dto.LoginRequest;
import com.example.demo.service.JwtService;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.AuthService;
import com.example.demo.model.AppUser;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final JwtService jwtService;

  public AuthController(AuthService authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @GetMapping("/me")
  public Map<String, Object> me(
    Authentication authentication
  ) {
    return Map.of(
      "username", authentication.getName(),
          "authorities", authentication.getAuthorities()
    );
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(
    @Valid @RequestBody RegisterRequest request
  ) {
    AppUser user = authService.register(request.username(), request.password());

    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
  }

  @PostMapping("/login")
public Map<String, String> login(
        @Valid @RequestBody LoginRequest request) {

    String token = authService.login(
            request.username(),
            request.password()
    );

    return Map.of(
            "accessToken", token
    );
}
}
