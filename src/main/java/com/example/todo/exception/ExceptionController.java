package com.example.todo.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.JwtException;

@ControllerAdvice
public class ExceptionController {
  @ExceptionHandler(TodoException.class)
  public ResponseEntity<ExceptionDto> handleTodoException(TodoException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(new ExceptionDto(Instant.now(), HttpStatus.BAD_REQUEST.value(), e.getError(), e.getMessage()));
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ExceptionDto> handleUsernotFoundException(UsernameNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(new ExceptionDto(Instant.now(), HttpStatus.NOT_FOUND.value(), "Usernot Found", e.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ExceptionDto> handleBadCredentialException(BadCredentialsException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(new ExceptionDto(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Bad Credential", e.getMessage()));
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ExceptionDto> handleJwtException(JwtException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(new ExceptionDto(Instant.now(), HttpStatus.UNAUTHORIZED.value(), "Jwt Forbidden", e.getMessage()));
  }
}
