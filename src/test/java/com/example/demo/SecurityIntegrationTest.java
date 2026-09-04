package com.example.demo;

import com.example.demo.model.AppUser;
import com.example.demo.repository.PlayerRepository;
import com.example.demo.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        playerRepository.deleteAll();
        userRepository.deleteAll();

        AppUser lucas = new AppUser(
                "lucas",
                passwordEncoder.encode("dragon123"),
                "ROLE_USER");

        userRepository.save(lucas);
    }
    
    @Test
    void getPlayers_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/players")).andExpect(status().isUnauthorized());
    }

    @Test
    void getPlayers_withValidToken_returns200() throws Exception {
        String token = loginAndGetToken("lucas", "dragon123");

        mockMvc.perform(get("/api/players")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)).andExpect(status().isOk());
    }
    
    @Test
    void me_withValidToken_returnsAuthenticatedUser() throws Exception {
        String token = loginAndGetToken("lucas", "dragon123");

        mockMvc.perform(
            get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("lucas"))
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    //Helper methods
    private String loginAndGetToken(
        String username,
                String password
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "username": "%s",
                            "password": "%s"
                }
                """.formatted(username, password))).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();

        Map<String, Object> response = JsonParserFactory.getJsonParser().parseMap(json);

        return (String) response.get("accessToken");
    }

    @Test
void deleteMissingPlayer_asUser_returns404() throws Exception {

    String token =
            loginAndGetToken("lucas", "dragon123");

    mockMvc.perform(
            delete("/api/players/999")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + token
                    )
    )
            .andExpect(status().isNotFound());
}
    
    @Test
    void deletePlayer_asAdmin_passesAuthorization() throws Exception {
        mockMvc.perform(
            delete("/api/players/999")
            .with(
                jwt().authorities(
                    new SimpleGrantedAuthority("ROLE_ADMIN")
                )
            )
        ).andExpect(status().isNotFound());
    }
}