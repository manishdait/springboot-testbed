package com.example.todo.exception;

import java.time.Instant;

public record ExceptionDto(Instant timestamp, Integer status, String error, String message) {
  
}
