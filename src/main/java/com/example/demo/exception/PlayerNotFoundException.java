package com.example.demo.exception;

public class PlayerNotFoundException extends RuntimeException {
  public PlayerNotFoundException(Long id) {
    super("Player with id " + id + " was not found.");
    }
}
