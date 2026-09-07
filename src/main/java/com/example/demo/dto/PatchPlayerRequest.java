package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PatchPlayerRequest(
    @Size(
    min = 1,
        max = 50,
            message = "Name must be between 1 and 50 characters."
  )
    String name,
    @Min(
        value = 1,
            message = "Level must be at least 1."
      )
      Integer level
) {
  
}
