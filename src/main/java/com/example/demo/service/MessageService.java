package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class MessageService {
  public String getHelloMessage() {
    return "Hello from the Service!!";
  }
}