package com.example.todo.user;

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
public class UserRepositoryTest extends TestUtils{
  @Container
  @ServiceConnection
  private static PostgreSQLContainer<?> pSqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setup() {
    User user = VALID_USER;
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
  void returns_UserOptional_ForExistingEmail() {
    String email = VALID_EMAIL;
    Optional<User> _user = userRepository.findByEmail(email);

    Assertions.assertThat(_user).isPresent();
    Assertions.assertThat(_user.get().getEmail()).isEqualTo(email);
  }

  @Test
  void returns_EmptyOptional_ForNonExistentEmail() {
    String email = INVALID_EMAIL;
    Optional<User> _user = userRepository.findByEmail(email);

    Assertions.assertThat(_user).isEmpty();
  }

  @Test
  void returns_UserOptional_ForExistingUuid() {
    String uuid = VALID_USER_UUID;
    Optional<User> _user = userRepository.findByUuid(uuid);

    Assertions.assertThat(_user).isPresent();
    Assertions.assertThat(_user.get().getEmail()).isEqualTo(VALID_EMAIL);
  }

  @Test
  void returns_EmptyOptional_ForNonExistentUuid() {
    String uuid = UUID.randomUUID().toString();
    Optional<User> _user = userRepository.findByUuid(uuid);

    Assertions.assertThat(_user).isEmpty();
  }
}
