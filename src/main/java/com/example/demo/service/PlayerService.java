package com.example.demo.service;

import com.example.demo.exception.PlayerNotFoundException;
import com.example.demo.model.Player;
import com.example.demo.model.AppUser;
import com.example.demo.repository.PlayerRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
  private final PlayerRepository playerRepository;
  private final UserRepository userRepository;

  public PlayerService(PlayerRepository playerRepository, UserRepository userRepository) {
    this.playerRepository = playerRepository;
    this.userRepository = userRepository;
  }

  public Page<Player> getPlayersForUser(String username, String name, Pageable pageable) {
    if (name == null || name.isBlank()) {
      return playerRepository.findByOwner_Username(username, pageable);
    }


    return playerRepository.findByOwner_UsernameAndNameContainingIgnoreCase(username,name, pageable);
  }

  public Player createPlayer(String name, int level, String username) {
    AppUser owner = userRepository.findByUsername(username).orElseThrow();

    Player player = new Player();

    player.setName(name);
    player.setLevel(level);
    player.setOwner(owner);


    return playerRepository.save(player);
  }

  public Player getPlayerById(Long id) {
    return playerRepository.findById(id)
        .orElseThrow(() -> new PlayerNotFoundException(id));
  }
  
  public Player getPlayerForUser(
    Long id, String username
  ) {
    return playerRepository
    .findByIdAndOwner_Username(id, username)
        .orElseThrow(
          () -> new PlayerNotFoundException(id)
        );
  }

  public Player updatePlayerForUser(Long id, String name, int level,String username) {
    Player player = playerRepository.findByIdAndOwner_Username(id, username)
        .orElseThrow(() -> new PlayerNotFoundException(id));

    player.setName(name);
    player.setLevel(level);
        
    return playerRepository.save(player);
  }
  
  public void deletePlayerForUser(
    Long id,
    String username,
        boolean isAdmin
  ) {
    Player player;

    if (isAdmin) {
      player = playerRepository.findById(id)
          .orElseThrow(() -> new PlayerNotFoundException(id));
    } else {
      player = playerRepository
          .findByIdAndOwner_Username(id, username)
          .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    playerRepository.delete(player);
  }
  
  @Transactional
  public Player addExperienceForUser(
    Long id,
        int amount,
            String username
  ) {
    Player player = playerRepository.findByIdAndOwner_Username(id, username)
        .orElseThrow(() -> new PlayerNotFoundException(id));

    player.setExperience(
        player.getExperience() + amount);

    while (player.getExperience() >= experienceNeeded(player.getLevel())) {
      int required = experienceNeeded(player.getLevel());

      player.setExperience(player.getExperience() - required);

      player.setLevel(player.getLevel() + 1);
    }

    return playerRepository.save(player);
  }
  
  public Player patchPlayerForUser(
    Long id,
        String name,
            Integer level,
                String username
  ) {
    Player player = playerRepository.findByIdAndOwner_Username(id, username)
        .orElseThrow(() -> new PlayerNotFoundException(id));

    if (name != null) {
      player.setName(name);
    }
        
    if (level != null) {
      player.setLevel(level);
    }

    return playerRepository.save(player);
  }
  
  //This is helper function
  private int experienceNeeded(int level) {
    return level * 100;
  }
}
