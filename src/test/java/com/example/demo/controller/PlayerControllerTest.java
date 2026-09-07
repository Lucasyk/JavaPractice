package com.example.demo.controller;

import com.example.demo.model.Player;
import com.example.demo.service.PlayerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Test
void getPlayers_returnsPlayers() throws Exception {

    Player player = new Player("Knight", 1);

    Page<Player> page =
            new PageImpl<>(List.of(player));

    given(
        playerService.getPlayersForUser(
            eq("lucas"),
            isNull(),
            any(Pageable.class)
        )
    ).willReturn(page);

    Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                    "lucas",
                    null,
                    List.of(
                            new SimpleGrantedAuthority("ROLE_USER")
                    )
            );

    mockMvc.perform(
            get("/api/players")
                    .principal(authentication)
    )
            .andExpect(status().isOk())
            .andExpect(
                    jsonPath("$.content[0].name")
                            .value("Knight")
            );
}
}