package com.example.todo.exception;

import lombok.Getter;

@Getter
public class TodoException extends RuntimeException {
  String error;
  private String message;

  public TodoException(String error, String message) {
    super(message);
    this.message = message;
    this.error = error;
  }
}
