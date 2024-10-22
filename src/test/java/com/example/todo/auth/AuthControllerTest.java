package com.example.todo.auth;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.todo.user.User;
import com.example.todo.user.UserRepository;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {
  @Container
  @ServiceConnection
  private static PostgreSQLContainer<?> pSqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @BeforeEach
  void setup() {
    User user = User.builder()
      .uuid(UUID.randomUUID().toString())
      .email("jhondoe@test.in")
      .password(passwordEncoder.encode("Jhon@24"))
      .build();

    userRepository.save(user);
  }

  @AfterEach
  void purge() {
    userRepository.deleteAll();
  }

  @Test
  void connectionIsEstablished() {
    Assertions.assertThat(pSqlContainer.isCreated()).isTrue();
    Assertions.assertThat(pSqlContainer.isRunning()).isTrue();
  }

  @Test
  void should_ReturnCreated_WhenSignUp_IfUsernotExist() {
    //given
    AuthRequestDto request = new AuthRequestDto("katedoe@test.in", "Kate@24");

    //when
    ResponseEntity<AuthResponseDto> response = testRestTemplate.exchange(
      "/todo-api/v1/auth/sign-up", 
      HttpMethod.POST, 
      new HttpEntity<>(request), 
      AuthResponseDto.class
    );

    //then
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody().username()).isEqualTo(request.email());
    Assertions.assertThat(response.getBody().authToken()).isNotNull();
  }

  @Test
  void should_ReturnBadRequest_WhenSignUp_IfUserAlreadyExist() {
    //given
    AuthRequestDto request = new AuthRequestDto("jhondoe@test.in", "Jhon@24");

    //when
    ResponseEntity<AuthResponseDto> response = testRestTemplate.exchange(
      "/todo-api/v1/auth/sign-up", 
      HttpMethod.POST, 
      new HttpEntity<>(request), 
      AuthResponseDto.class
    );

    //then
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Assertions.assertThat(response.getBody()).isEqualTo(new AuthResponseDto(null, null));
  }

  @Test
  void should_ReturnOk_WhenLogin_ForValidCredentials() {
    //given
    AuthRequestDto request = new AuthRequestDto("jhondoe@test.in", "Jhon@24");

    //when
    ResponseEntity<AuthResponseDto> response = testRestTemplate.exchange(
      "/todo-api/v1/auth/login",
      HttpMethod.POST,
      new HttpEntity<>(request),
      AuthResponseDto.class
    );

    //then
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody().username()).isEqualTo(request.email());
    Assertions.assertThat(response.getBody().authToken()).isNotNull();
  }

  @Test
  void should_ReturnBadRequest_WhenLogin_ForInvalidCredentials() {
    //given
    AuthRequestDto request = new AuthRequestDto("timcook@test.in", "Tim@24");

    //when
    ResponseEntity<AuthResponseDto> response = testRestTemplate.exchange(
      "/todo-api/v1/auth/login",
      HttpMethod.POST,
      new HttpEntity<>(request),
      AuthResponseDto.class
    );

    //then
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Assertions.assertThat(response.getBody()).isEqualTo(new AuthResponseDto(null, null));
  }
}
