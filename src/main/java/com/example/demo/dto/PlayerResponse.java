package com.example.demo.dto;

import com.example.demo.model.Player;

public record PlayerResponse(
  Long id,
      String name,
          int level,
              int experience
) {
  public static PlayerResponse from(Player player) {
    return new PlayerResponse(player.getId(), player.getName(), player.getLevel(), player.getExperience());
 } 
}
