package com.example.demo;

import com.example.demo.model.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.withSettings;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.repository.PlayerRepository;
import com.example.demo.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("tc")
@Import(TestContainerConfiguration.class)
class PlayerIntegrationTest {
  
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setup() {
    playerRepository.deleteAll();
    userRepository.deleteAll();

    AppUser lucas = new AppUser(
      "lucas",
          "test-password-hash",
              "ROLE_USER"
    );

    userRepository.save(lucas);
  }

  @Test
  void createPlayer_thenGetPlayers_returnsSavedPlayer() throws Exception {

    mockMvc.perform(
        post("/api/players").with(jwt().jwt(jwt -> jwt.subject("lucas")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Lucas",
                  "level": 1
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Lucas"))
        .andExpect(jsonPath("$.level").value(1))
        .andExpect(jsonPath("$.createdAt").exists());

    mockMvc.perform(get("/api/players").with(jwt().jwt(jwt -> jwt.subject("lucas"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Lucas"))
        .andExpect(jsonPath("$.content[0].level").value(1));
  }

  @Test
  void getPlayers_largePageSize_isCappedAt100() throws Exception {
    mockMvc.perform(
        get("/api/players?page=0&size=10000")
            .with(jwt().jwt(jwt -> jwt.subject("lucas"))))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(100));
  }
  
  @Test
  void getPlayers_invalidSortField_returns400() throws Exception {

    mockMvc.perform(
        get("/api/players?sort=banana,asc")
            .with(jwt().jwt(jwt -> jwt.subject("lucas"))))

        .andExpect(status().isBadRequest());
  }
  
  @Test
  void getPlayers_validSortField_returns200() throws Exception {
    mockMvc.perform(
        get("/api/players?sort=level,desc")
            .with(jwt().jwt(jwt -> jwt.subject("lucas"))))
        .andExpect(status().isOk());
  }
  
  @Test
void getPlayers_returnsSliceWithNextPage() throws Exception {

    AppUser lucas = userRepository.findByUsername("lucas")
            .orElseThrow();

    Player player1 = new Player("Knight", 1);
    player1.setOwner(lucas);

    Player player2 = new Player("Wizard", 1);
    player2.setOwner(lucas);

    playerRepository.saveAndFlush(player1);
    playerRepository.saveAndFlush(player2);

    mockMvc.perform(
            get("/api/players?page=0&size=1")
                    .with(jwt().jwt(jwt -> jwt.subject("lucas")))
    )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.last").value(false));
}
}
