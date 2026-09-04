package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpHeaders;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.repository.PlayerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.AppUser;
import com.example.demo.model.Player;

/**
 * OwnershipIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OwnershipIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private Player lucasPlayer;
  private Player bobPlayer;

  @BeforeEach
  void setup() {
    playerRepository.deleteAll();
    userRepository.deleteAll();

    AppUser lucas = new AppUser(
        "lucas",
        passwordEncoder.encode("dragon123"),
        "ROLE_USER");

    AppUser bob = new AppUser(
        "bob",
        passwordEncoder.encode("bobpass123"),
        "ROLE_USER");

    AppUser admin = new AppUser(
        "admin",
        passwordEncoder.encode("adminpass123"),
        "ROLE_ADMIN");

    userRepository.save(lucas);
    userRepository.save(bob);
    userRepository.save(admin);

    lucasPlayer = new Player("Knight", 1);
    lucasPlayer.setOwner(lucas);

    bobPlayer = new Player("Darkwizard", 1);
    bobPlayer.setOwner(bob);

    lucasPlayer = playerRepository.save(lucasPlayer);
    bobPlayer = playerRepository.save(bobPlayer);
  }
  
  private String loginAndGetToken(
    String username,
        String password
  ) throws Exception {
    MvcResult result = mockMvc.perform(
        post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                      "username": "%s",
                          "password": "%s"
                    }
                    """.formatted(username, password)))
        .andExpect(status().isOk())
        .andReturn();

    String json = result.getResponse().getContentAsString();

    Map<String, Object> response = JsonParserFactory
        .getJsonParser()
        .parseMap(json);

    return (String) response.get("accessToken");
  }
  
  
  @Test
  void lucas_onlySeesHisOwnPlayers() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
      get("/api/players")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
    )
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Knight"));
  }
  
  @Test
  void lucas_cannotReadBobsPlayer() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        get("/api/players/" + bobPlayer.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isNotFound());
  }
  
  @Test
  void lucas_cannotUpdateBobsPlayer() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        put("/api/players/" + bobPlayer.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                      "name" : "BobIsNowAChicken",
                      "level" : 99
                    }
                    """))
        .andExpect(status().isNotFound());

    Player unchanged = playerRepository.findById(bobPlayer.getId()).orElseThrow();

    assertEquals("Darkwizard", unchanged.getName());
    assertEquals(1, unchanged.getLevel());
  }
  
  @Test
  void lucas_cannotDeleteBobsPlayer() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        delete("/api/players/" + bobPlayer.getId())
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token))
        .andExpect(status().isNotFound());

    boolean stillExists = playerRepository.existsById(bobPlayer.getId());

    assertTrue(stillExists);
  }
  
  @Test
  void lucas_canDeleteHisOwnPlayer() throws Exception {

    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        delete("/api/players/" + lucasPlayer.getId())
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token))
        .andExpect(status().isNoContent());

    boolean stillExists = playerRepository.existsById(lucasPlayer.getId());

    assertFalse(stillExists);
  }

  @Test
void admin_canDeleteBobsPlayer() throws Exception {

    String token =
            loginAndGetToken("admin", "adminpass123");

    mockMvc.perform(
            delete("/api/players/" + bobPlayer.getId())
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + token
                    )
    )
            .andExpect(status().isNoContent());

    boolean stillExists =
            playerRepository.existsById(bobPlayer.getId());

    assertFalse(stillExists);
}
}