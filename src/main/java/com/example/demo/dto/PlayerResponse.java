package com.example.demo.dto;

import com.example.demo.model.Player;

import java.time.Instant;

public record PlayerResponse(
  Long id,
      String name,
          int level,
              int experience,
                  Instant createdAt
) {
  public static PlayerResponse from(Player player) {
    return new PlayerResponse(player.getId(), player.getName(), player.getLevel(), player.getExperience(), player.getCreatedAt());
 } 
}
