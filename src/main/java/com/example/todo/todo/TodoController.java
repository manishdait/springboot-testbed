package com.example.todo.todo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/todo-api/v1/todo")
@AllArgsConstructor
public class TodoController {
  private final TodoServcie todoServcie;

  @GetMapping()
  public ResponseEntity<List<TodoResponseDto>> getTodos() {
    return ResponseEntity.status(HttpStatus.OK).body(todoServcie.getTodos());
  }

  @GetMapping("/{id}")
  public ResponseEntity<TodoResponseDto> getTodo(@PathVariable(name = "id") String id) {
    return ResponseEntity.status(HttpStatus.OK).body(todoServcie.getTodo(id));
  }

  @PostMapping()
  public ResponseEntity<TodoResponseDto> addTodo(@RequestBody TodoRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(todoServcie.addTodo(request));
  }

  @PutMapping("/{id}/status/{status}")
  public ResponseEntity<TodoResponseDto> changeStatus(@PathVariable(name = "id") String id, @PathVariable(name = "status") Status status) {
    return ResponseEntity.status(HttpStatus.OK).body(todoServcie.update(id, status));
  }

  @PutMapping("/{id}")
  public ResponseEntity<TodoResponseDto> update(@PathVariable(name = "id") String id, @RequestBody TodoRequestDto request) {
    return ResponseEntity.status(HttpStatus.OK).body(todoServcie.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable(name = "id") String id) {
    todoServcie.delete(id);
    return ResponseEntity.status(HttpStatus.OK).body(null);
  }
}
