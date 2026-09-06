package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.exception.PlayerNotFoundException;
import com.example.demo.model.AppUser;
import com.example.demo.model.Player;
import com.example.demo.repository.PlayerRepository;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {
  @Mock
  private PlayerRepository playerRepository;

  @Mock
  private UserRepository userRepository;
  
  @InjectMocks
  private PlayerService playerService;

  @Test
  void getPlayerById_whenPlayerExists_returnsPlayer() {
    Player player = new Player("Lucas", 10);

    given(playerRepository.findById(1L)).willReturn(Optional.of(player));

    Player result = playerService.getPlayerById(1L);

    assertEquals("Lucas", result.getName());
    assertEquals(10, result.getLevel());
  }

  @Test
  void getPlayerById_whenPlayerDoesNotExist_throwException() {
    given(playerRepository.findById(999L)).willReturn(Optional.empty());

    assertThrows(PlayerNotFoundException.class, () -> playerService.getPlayerById(999L));
  }
  
  @Test
void createPlayer_savesPlayer() {

    AppUser lucas = new AppUser(
            "lucas",
            "hashed-password",
            "ROLE_USER"
    );

    given(userRepository.findByUsername("lucas"))
            .willReturn(Optional.of(lucas));

    given(playerRepository.save(any(Player.class)))
            .willAnswer(invocation ->
                    invocation.getArgument(0)
            );

    Player result =
            playerService.createPlayer(
                    "Lucas",
                    1,
                    "lucas"
            );

    assertEquals("Lucas", result.getName());
    assertEquals(1, result.getLevel());
    assertEquals(lucas, result.getOwner());
}
  
  @Test
void updatePlayerForUser_updatesOwnedPlayer() {

    Player player = new Player("Lucas", 1);

    given(
            playerRepository.findByIdAndOwner_Username(
                    1L,
                    "lucas"
            )
    )
            .willReturn(Optional.of(player));

    given(playerRepository.save(player))
            .willReturn(player);

    Player result =
            playerService.updatePlayerForUser(
                    1L,
                    "Mega Lucas",
                    20,
                    "lucas"
            );

    assertEquals("Mega Lucas", result.getName());
    assertEquals(20, result.getLevel());
}

  @Test
void updatePlayerForUser_missingPlayer_throwsException() {

    given(
            playerRepository.findByIdAndOwner_Username(
                    999L,
                    "lucas"
            )
    )
            .willReturn(Optional.empty());

    assertThrows(
            PlayerNotFoundException.class,
            () -> playerService.updatePlayerForUser(
                    999L,
                    "Ghost Player",
                    10,
                    "lucas"
            )
    );
}

  @Test
void deletePlayerForUser_deletesOwnedPlayer() {

    Player player = new Player("Lucas", 1);

    given(
            playerRepository.findByIdAndOwner_Username(
                    1L,
                    "lucas"
            )
    )
            .willReturn(Optional.of(player));

    playerService.deletePlayerForUser(
            1L,
            "lucas",
            false
    );

    verify(playerRepository).delete(player);
}

  @Test
  void deletePlayerForUser_missingPlayer_throwsException() {

    given(
        playerRepository.findByIdAndOwner_Username(
            999L,
            "lucas"))
        .willReturn(Optional.empty());

    assertThrows(
        PlayerNotFoundException.class,
        () -> playerService.deletePlayerForUser(
            999L,
            "lucas",
            false));
  }

  @Test
  void addExperience_withoutLevelUp_increasesExperience() {
    Player player = new Player("Knight", 1);

    given(
        playerRepository.findByIdAndOwner_Username(1L, "lucas")).willReturn(Optional.of(player));

    given(playerRepository.save(player)).willReturn(player);

    Player result = playerService.addExperienceForUser(1L, 50, "lucas");

    assertEquals(1, result.getLevel());
    assertEquals(50, result.getExperience());
  }
  
  @Test
  void addExperience_enoughForOneLevelUp_levelsUp() {
    Player player = new Player("Knight", 1);

    given(
        playerRepository.findByIdAndOwner_Username(1L, "lucas")).willReturn(Optional.of(player));

    given(playerRepository.save(player)).willReturn(player);

    Player result = playerService.addExperienceForUser(1L, 250, "lucas");

    assertEquals(2, result.getLevel());
    assertEquals(150, result.getExperience());
  }
  
  @Test
  void addExperience_largeAmount_canLevelUpMultipleTimes() {
    Player player = new Player("Knight", 1);

    given(
        playerRepository.findByIdAndOwner_Username(1L, "lucas")).willReturn(Optional.of(player));

    given(
        playerRepository.save(player)).willReturn(player);

    Player result = playerService.addExperienceForUser(1L, 350, "lucas");

    assertEquals(3, result.getLevel());
    assertEquals(50, result.getExperience());
  }
  
  @Test
  void addExperience_toAnotherUsersPlayer_throwsException() {
    given(playerRepository.findByIdAndOwner_Username(
      5L,
          "lucas"
    )).willReturn(Optional.empty());

    assertThrows(PlayerNotFoundException.class,  ()->playerService.addExperienceForUser(5L, 100, "lucas"));
  }

}
