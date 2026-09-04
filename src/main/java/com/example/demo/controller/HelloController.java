package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.service.MessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  private final MessageService messageService;

  public HelloController(MessageService messageService) {
    this.messageService = messageService;
  }

    @GetMapping("/api/hello")
    public Message hello() {
        return new Message(messageService.getHelloMessage());
    }
}