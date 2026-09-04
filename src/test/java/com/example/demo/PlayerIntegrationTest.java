package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.model.AppUser;
import com.example.demo.repository.PlayerRepository;
import com.example.demo.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
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

  @BeforeEach
  void cleanDatabase() {
    playerRepository.deleteAll();
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
                            """)
    )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Lucas"))
            .andExpect(jsonPath("$.level").value(1));

    mockMvc.perform(get("/api/players").with(jwt().jwt(jwt -> jwt.subject("lucas"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Lucas"))
            .andExpect(jsonPath("$[0].level").value(1));
}
}
