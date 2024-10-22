package com.example.todo.todo;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.todo.auth.AuthRequestDto;
import com.example.todo.auth.AuthResponseDto;
import com.example.todo.user.User;
import com.example.todo.user.UserRepository;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.jpa.show-sql=false"})
public class TodoControllerTest {
  @Container
  @ServiceConnection
  private static PostgreSQLContainer<?> pSqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TodoRepository todoRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @BeforeEach
  void setup() {
    String uuid = UUID.randomUUID().toString();
    User user = User.builder()
      .uuid(uuid)
      .email("jhondoe@test.in")
      .password(passwordEncoder.encode("Jhon@24"))
      .build();

    userRepository.save(user);
  }

  @AfterEach
  void purge() {
    todoRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void connectionIsEstablished() {
    Assertions.assertThat(pSqlContainer.isCreated()).isTrue();
    Assertions.assertThat(pSqlContainer.isRunning()).isTrue();
  }

  AuthResponseDto authenticate() {
    AuthRequestDto loginRequest = new AuthRequestDto("jhondoe@test.in", "Jhon@24");
    ResponseEntity<AuthResponseDto> authResponse = testRestTemplate.exchange(
      "/todo-api/v1/auth/login",
      HttpMethod.POST, 
      new HttpEntity<>(loginRequest), 
      AuthResponseDto.class
    );

    return authResponse.getBody();
  }

  @Test
  void should_ReturnCreated_WhenAddTodo_IfUserIsAuthenticated() {
    AuthResponseDto authResponse = authenticate();

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authResponse.authToken());

    TodoRequestDto request = new TodoRequestDto("demo todo by user");
    ResponseEntity<TodoResponseDto> response = testRestTemplate.exchange(
      "/todo-api/v1/todo",
      HttpMethod.POST, 
      new HttpEntity<>(request, httpHeaders), 
      TodoResponseDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody().title()).isEqualTo(request.title());
    Assertions.assertThat(response.getBody().status()).isEqualTo(Status.PENDING);
  }

  @Test
  void should_ReturnListOfTodos_ForUser() {
    AuthResponseDto authResponse = authenticate();

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authResponse.authToken());

    ResponseEntity<List<TodoResponseDto>> response = testRestTemplate.exchange(
      "/todo-api/v1/todo",
      HttpMethod.GET, 
      new HttpEntity<>(httpHeaders), 
      new ParameterizedTypeReference<List<TodoResponseDto>>(){}
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void should_ReturnTodo_ForId() {
    AuthResponseDto authResponse = authenticate();

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authResponse.authToken());

    TodoRequestDto todo = new TodoRequestDto("demo todo by user");
    ResponseEntity<TodoResponseDto> todoResponse = testRestTemplate.exchange(
      "/todo-api/v1/todo",
      HttpMethod.POST, 
      new HttpEntity<>(todo, httpHeaders), 
      TodoResponseDto.class
    );

    ResponseEntity<TodoResponseDto> response = testRestTemplate.exchange(
      "/todo-api/v1/todo/" + todoResponse.getBody().uuid(),
      HttpMethod.GET, 
      new HttpEntity<>(httpHeaders), 
      TodoResponseDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().title()).isEqualTo(todoResponse.getBody().title());
  }

  @Test
  void should_ReturnTodo_WithUpdatedStatus_ForId() {
    AuthResponseDto authResponse = authenticate();

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authResponse.authToken());

    TodoRequestDto todo = new TodoRequestDto("demo todo by user");
    ResponseEntity<TodoResponseDto> todoResponse = testRestTemplate.exchange(
      "/todo-api/v1/todo",
      HttpMethod.POST, 
      new HttpEntity<>(todo, httpHeaders), 
      TodoResponseDto.class
    );

    Status status = Status.COMPLETED;
    ResponseEntity<TodoResponseDto> response = testRestTemplate.exchange(
      "/todo-api/v1/todo/" + todoResponse.getBody().uuid() + "/status/" + status,
      HttpMethod.PUT, 
      new HttpEntity<>(httpHeaders), 
      TodoResponseDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().title()).isEqualTo(todoResponse.getBody().title());
    Assertions.assertThat(response.getBody().status()).isEqualTo(status);
  }

  @Test
  void should_ReturnTodo_WithUpdatedTodo_ForId() {
    AuthResponseDto authResponse = authenticate();

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authResponse.authToken());


    TodoRequestDto todo = new TodoRequestDto("demo todo by user");
    ResponseEntity<TodoResponseDto> todoResponse = testRestTemplate.exchange(
      "/todo-api/v1/todo",
      HttpMethod.POST, 
      new HttpEntity<>(todo, httpHeaders), 
      TodoResponseDto.class
    );

    
    TodoRequestDto updatedTodo = new TodoRequestDto("updated todo by user");
    ResponseEntity<TodoResponseDto> response = testRestTemplate.exchange(
      "/todo-api/v1/todo/" + todoResponse.getBody().uuid(),
      HttpMethod.PUT, 
      new HttpEntity<>(updatedTodo, httpHeaders), 
      TodoResponseDto.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().title()).isEqualTo(updatedTodo.title());
  }

  @Test
  void should_deleteTodo_ForId() {
    AuthResponseDto authResponse = authenticate();

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(authResponse.authToken());


    TodoRequestDto todo = new TodoRequestDto("demo todo by user");
    ResponseEntity<TodoResponseDto> todoResponse = testRestTemplate.exchange(
      "/todo-api/v1/todo",
      HttpMethod.POST, 
      new HttpEntity<>(todo, httpHeaders), 
      TodoResponseDto.class
    );

    
    ResponseEntity<Void> response = testRestTemplate.exchange(
      "/todo-api/v1/todo/" + todoResponse.getBody().uuid(),
      HttpMethod.DELETE, 
      new HttpEntity<>(httpHeaders), 
      Void.class
    );

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
