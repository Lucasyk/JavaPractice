package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpHeaders;
import java.util.Map;

import javax.print.attribute.standard.Media;

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
import static org.mockito.Mockito.never;

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
    .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Knight"))
        .andExpect(jsonPath("$.totalElements").value(1));
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

    String token = loginAndGetToken("admin", "adminpass123");

    mockMvc.perform(
        delete("/api/players/" + bobPlayer.getId())
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token))
        .andExpect(status().isNoContent());

    boolean stillExists = playerRepository.existsById(bobPlayer.getId());

    assertFalse(stillExists);
  }

  @Test
  void lucas_canGainExperienceOnHisPlayer() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        post("/api/players/" + lucasPlayer.getId() + "/experience")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                    "amount" : 250
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.level").value(2))
        .andExpect(jsonPath("$.experience").value(150));

    Player updated = playerRepository.findById(lucasPlayer.getId()).orElseThrow();

    assertEquals(2, updated.getLevel());
    assertEquals(150, updated.getExperience());
  }
  
  @Test
  void negativeExperience_returns400() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        post("/api/players/" + lucasPlayer.getId() + "/experience")
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                  "amount":-100
                  }
                """))
        .andExpect(status().isBadRequest());

    Player unchanged = playerRepository.findById(lucasPlayer.getId()).orElseThrow();

    assertEquals(1, unchanged.getLevel());
    assertEquals(0, unchanged.getExperience());
  }
  
  @Test
  void lucas_cannotGiveExperienceToBobsPlayer() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        post("/api/players/" + bobPlayer.getId() + "/experience")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                 "amount":500
                  }
                """))
        .andExpect(status().isNotFound());

    Player unchanged = playerRepository.findById(bobPlayer.getId()).orElseThrow();

    assertEquals(1, unchanged.getLevel());
    assertEquals(0, unchanged.getExperience());
  }
  
  @Test
  void lucas_canPatchOnlyPlayerName() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        patch("/api/players/" + lucasPlayer.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                  "name": "Super Knight"
                  }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Super Knight"))
        .andExpect(jsonPath("$.level").value(1));

    Player updated = playerRepository.findById(lucasPlayer.getId()).orElseThrow();

    assertEquals("Super Knight", updated.getName());
    assertEquals(1, updated.getLevel());
  }
  
  @Test
  void lucas_canPatchOnlyPlayerLevel() throws Exception {

    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        patch("/api/players/" + lucasPlayer.getId())
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "level": 5
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Knight"))
        .andExpect(jsonPath("$.level").value(5));

    Player updated = playerRepository.findById(lucasPlayer.getId())
        .orElseThrow();

    assertEquals("Knight", updated.getName());
    assertEquals(5, updated.getLevel());
  }

  @Test
  void lucas_cannotPatchBobsPlayer() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        patch("/api/players" + bobPlayer.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                  "name":"Hacker",
                      "level":99
                  }
                """))
        .andExpect(status().isNotFound());

    Player unchanged = playerRepository.findById(bobPlayer.getId()).orElseThrow();

    assertEquals("Darkwizard", unchanged.getName());
    assertEquals(1, unchanged.getLevel());
  }
  
  @Test
  void patchPlayer_withInvalidLevel_returns400() throws Exception {
    String token = loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
        patch("/api/players/" + lucasPlayer.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "level": 0
                }
                """))
        .andExpect(status().isBadRequest());

    Player unchanged = playerRepository.findById(lucasPlayer.getId()).orElseThrow();

    assertEquals(1, unchanged.getLevel());
  }
}