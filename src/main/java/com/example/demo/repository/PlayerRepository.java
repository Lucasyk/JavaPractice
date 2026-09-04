package com.example.demo.repository;

import com.example.demo.model.Player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;;

public interface PlayerRepository extends JpaRepository<Player, Long> {
  List<Player> findByOwner_Username(String username);

  Optional<Player> findByIdAndOwner_Username(
    Long id, String username
  );
}
