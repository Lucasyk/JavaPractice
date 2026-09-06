package com.example.demo.dto;

import jakarta.validation.constraints.Min;

public record AddExperienceRequest(
  @Min(value = 1, message = "Experience amount must be at least 1.")
  int amount
) {
  
}
