package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.jdbc.core.JdbcTemplate;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.example.demo.repository.PlayerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.AppUser;
import com.example.demo.model.Player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.transaction.annotation.Transactional;

@Testcontainers
@SpringBootTest
@ActiveProfiles("tc")
class PostgreSqlContainerTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlayerRepository playerRepository;

  @Test
  void connectsToTemporaryPostgres() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      String database = connection.getMetaData().getDatabaseProductName();

      assertEquals("PostgreSQL", database);
    }
  }
  
  @Test
  void flywayCreatedPlayerTable() {
    String tableName = jdbcTemplate.queryForObject("""
        SELECT to_regclass('public.player')
        """, String.class);

    assertEquals("player", tableName);
  }
  
  @Test
  void flywayCreatedAppUserTable() {
    String tableName = jdbcTemplate.queryForObject("""
        SELECT to_regclass('public.app_user')
        """, String.class);

    assertEquals("app_user", tableName);
  }
  
  @Test
  @Transactional
  void canSaveAndFindPlayerByOwner() {
    
    AppUser lucas = new AppUser(
      "lucas",
          "fake-hash",
              "ROLE_USER"
    );

    lucas = userRepository.save(lucas);

    Player knight = new Player(
      "Knight",
          1
    );

    knight.setOwner(lucas);

    playerRepository.save(knight);

    Player saved = playerRepository.findByIdAndOwner_Username(knight.getId(), "lucas").orElseThrow();

    assertEquals("Knight", saved.getName());
    assertEquals("lucas", saved.getOwner().getUsername());
  }
}
