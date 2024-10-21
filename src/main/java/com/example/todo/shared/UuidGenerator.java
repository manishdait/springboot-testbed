package com.example.todo.shared;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UuidGenerator {
  public String randomUUID() {
    return UUID.randomUUID().toString();
  }
}
