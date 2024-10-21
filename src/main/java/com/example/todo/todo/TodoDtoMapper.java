package com.example.todo.todo;

import org.springframework.stereotype.Component;

@Component
public class TodoDtoMapper {
  public TodoResponseDto todoToDto(Todo todo) {
    return new TodoResponseDto(todo.getUuid(), todo.getTitle(), todo.getStatus(), todo.getCreatedAt(), todo.getUpdatedAt());
  }
}
