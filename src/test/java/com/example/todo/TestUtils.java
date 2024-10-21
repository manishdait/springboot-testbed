package com.example.todo;

import java.util.UUID;

import com.example.todo.todo.Status;
import com.example.todo.todo.Todo;
import com.example.todo.user.User;

public class TestUtils {
  public final String VALID_EMAIL = "jhondoe@testco.in";
  public final String INVALID_EMAIL = "katedoe@testco.in";

  public final String VALID_USER_UUID = UUID.randomUUID().toString();
  public final String INVALID_USER_UUID = UUID.randomUUID().toString();
  public final String VALID_TODO_UUID = UUID.randomUUID().toString();

  public final User VALID_USER = User.builder()
    .uuid(VALID_USER_UUID)
    .email(VALID_EMAIL)
    .password("Jhon@24")
    .build();

  public final User INVALID_USER = User.builder()
    .id(1L)
    .uuid(INVALID_USER_UUID)
    .email(INVALID_EMAIL)
    .password("Kate@24")
    .build();

  public final Todo TODO = Todo.builder()
    .uuid(VALID_TODO_UUID)
    .title("First Todo of the List.")
    .status(Status.PENDING)
    .user(VALID_USER)
    .build();
}
