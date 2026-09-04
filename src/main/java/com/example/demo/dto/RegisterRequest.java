package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
  @NotBlank(message = "username is required.")
  @Size(
    min = 3,
        max = 50,
            message = "Username must be between 3 and 50 characters."
  )
  String username,
          
  @NotBlank(message = "password is required.")
  @Size(
    min = 8,
        max = 100,
            message = "Password must be between 8 and 100 characters."
  )
  String password 
)
{}