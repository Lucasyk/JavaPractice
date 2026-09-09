package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatchPlayerRequest(
    @Size(
    min = 1,
        max = 50,
            message = "Name must be between 1 and 50 characters."
        )
        @Pattern(
    regexp = ".*\\S.*",
            message = "Name must not be blank."
  )
    String name,
    @Min(
        value = 1,
            message = "Level must be at least 1."
      )
      Integer level
) {
  
}
