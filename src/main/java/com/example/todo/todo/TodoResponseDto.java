package com.example.todo.todo;

import java.time.Instant;

public record TodoResponseDto(String uuid, String title, Status status, Instant createdAt, Instant updatedAt) {
  
}
