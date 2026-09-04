package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdatePlayerRequest(
  @NotBlank(message = "Name is required")
    String name,
      @Min(value = 1, message = "Level must be at least 1.")
      int level
){
  
}
