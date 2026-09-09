package com.example.demo.controller;

import com.example.demo.dto.CreatePlayerRequest;
import com.example.demo.dto.PatchPlayerRequest;
import com.example.demo.dto.UpdatePlayerRequest;
import com.example.demo.dto.AddExperienceRequest;
import com.example.demo.dto.PlayerResponse;
import com.example.demo.model.Player;
import com.example.demo.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;


import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
  private final PlayerService playerService;

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
    "id",
        "name",
            "level",
                "experience",
                    "createdAt"
  ); 

  public PlayerController(PlayerService playerService) {
    this.playerService = playerService;
  }

  @GetMapping
  public Slice<PlayerResponse> getPlayers(Authentication authentication, @RequestParam(required = false) String name,
      Pageable pageable) {
    
    for (Sort.Order order : pageable.getSort()) {
      if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
            throw new ResponseStatusException(BAD_REQUEST,
                  "Invalid sort field: " + order.getProperty()
            );
          }
        }

    return playerService.getPlayersForUser(authentication.getName(),name, pageable).map(PlayerResponse::from);
  }

  @GetMapping("/{id}")
  public PlayerResponse getPlayerById(
      @PathVariable Long id, Authentication authentication, Pageable pageable
  ) {
    Player player = playerService.getPlayerForUser(id,authentication.getName());
    return PlayerResponse.from(
      player
    );
  }

  @PostMapping
  public PlayerResponse createPlayer(@Valid @RequestBody CreatePlayerRequest request,
    Authentication authentication
  ) {
    Player player = playerService.createPlayer(
        request.name(),
        request.level(),
        authentication.getName());

    return PlayerResponse.from(player);
  }
  
  @PostMapping("/{id}/experience")
  public PlayerResponse addExperience(
    @PathVariable Long id,
        @Valid @RequestBody AddExperienceRequest request,
            Authentication authentication
  ) {
    Player player = playerService.addExperienceForUser(id, request.amount(), authentication.getName());

    return PlayerResponse.from(player);
  }

  @PutMapping("/{id}")
  public PlayerResponse updatePlayer(
      @PathVariable Long id,
      @Valid @RequestBody UpdatePlayerRequest request,
      Authentication authentication
  ) {

    Player player = playerService.updatePlayerForUser(id, request.name(), request.level(), authentication.getName());
    return PlayerResponse.from(player);
  }
  
  @PatchMapping("/{id}")
  public PlayerResponse patchPlayer(
    @PathVariable Long id,
        @Valid @RequestBody PatchPlayerRequest request,
            Authentication authentication
  ) {
    Player player = playerService.patchPlayerForUser(id, request.name(), request.level(), authentication.getName());

    return PlayerResponse.from(player);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePlayer(
      @PathVariable Long id, Authentication authentication
  ) {

    boolean isAdmin = authentication
    .getAuthorities()
    .stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

    playerService.deletePlayerForUser(id, authentication.getName(),isAdmin);

    return ResponseEntity.noContent().build();
  }
}