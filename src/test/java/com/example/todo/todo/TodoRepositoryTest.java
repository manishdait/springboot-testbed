package com.example.todo.todo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.todo.TestUtils;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class TodoRepositoryTest extends TestUtils {
  @Container
  @ServiceConnection
  private static PostgreSQLContainer<?> pSqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private TodoRepository todoRepository;

  @BeforeEach
  void setup() {
    todoRepository.save(TODO);
  }

  @AfterEach
  void purge() {
    todoRepository.deleteAll();
  }

  @Test
  void connectionIsEstablished() {
    Assertions.assertThat(pSqlContainer.isCreated()).isTrue();
    Assertions.assertThat(pSqlContainer.isRunning()).isTrue();
  }

  @Test
  void returns_TodoList_ForValidUser() {
    List<Todo> todos = todoRepository.findByUser(VALID_USER);

    Assertions.assertThat(todos).isNotNull();
    Assertions.assertThat(todos.size()).isEqualTo(1);
  }

  @Test
  void returns_EmptyList_ForInvalidUser() {
    List<Todo> todos = todoRepository.findByUser(INVALID_USER);

    Assertions.assertThat(todos).isEmpty();
    Assertions.assertThat(todos.size()).isEqualTo(0);
  }

  @Test
  void returns_TodoOptional_ForValidUuid() {
    Optional<Todo> todo = todoRepository.findByUuid(VALID_TODO_UUID);

    Assertions.assertThat(todo).isPresent();
    Assertions.assertThat(todo.get()).isEqualTo(TODO);
  }

  @Test
  void returns_EmptyOptional_ForInvalidUuid() {
    String uuid = UUID.randomUUID().toString();
    Optional<Todo> todo = todoRepository.findByUuid(uuid);

    Assertions.assertThat(todo).isEmpty();
  }
}
