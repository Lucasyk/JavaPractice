package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Player {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private int level;

  private int experience;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  private AppUser owner;

  public Player() { }
  
  public Player(String name, int level) {
    this.name = name;
    this.level = level;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getLevel() {
    return level;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getExperience() {
    return experience;
  }

  public void setExperience(int experience) {
    this.experience = experience;
  }

  public AppUser getOwner() {
    return owner;
  }

  public void setOwner(AppUser owner) {
    this.owner = owner;
  }
}
