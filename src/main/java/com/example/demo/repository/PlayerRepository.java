package com.example.demo.repository;

import com.example.demo.model.Player;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;;

public interface PlayerRepository extends JpaRepository<Player, Long> {
  Slice<Player> findByOwner_Username(String username, Pageable pageable);

  Slice<Player> findByOwner_UsernameAndNameContainingIgnoreCase(
    String username,
        String name,
            Pageable pageable
  );

  Optional<Player> findByIdAndOwner_Username(
    Long id, String username
  );

}
