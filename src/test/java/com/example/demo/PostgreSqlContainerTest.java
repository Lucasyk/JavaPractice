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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import javax.sql.DataSource;

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
}
