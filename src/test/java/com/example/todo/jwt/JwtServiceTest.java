package com.example.todo.jwt;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import com.example.todo.user.User;

import io.jsonwebtoken.JwtException;


@SpringBootTest
@TestPropertySource(properties = {"jwt.expiration-seconds=2"})
public class JwtServiceTest {
  @Autowired
  private JwtService jwtService;

  @Test
  void returnToken_ForGivenUserName() {
    String username = "jhondoe@test.in";
    String token = jwtService.generateToken(username);

    Assertions.assertThat(token).isNotNull();
    Assertions.assertThat(token.length()).isGreaterThan(0);
  }

  @Test
  void returnTrue_ForValidToken() {
    String username = "jhondoe@test.in";
    String token = jwtService.generateToken(username);

    UserDetails userDetails = new User(null, null, null, username);
    Assertions.assertThat(jwtService.isValid(userDetails, token)).isTrue();
  }

  @Test
  void returnFalse_ForInValidUsername() {
    String username = "jhondoe@test.in";
    String token = jwtService.generateToken(username);

    UserDetails userDetails = new User(null, null, null, "katedoe@test.in");
    Assertions.assertThat(jwtService.isValid(userDetails, token)).isFalse();
  }

  @Test
  void throw_JwtException_ForInvalidToken() throws InterruptedException {
    String username = "jhondoe@test.in";
    String token = jwtService.generateToken(username);
    Thread.sleep(2000);

    UserDetails userDetails = new User(null, null, null, "jhondoe@test.in");
    Assertions.assertThatThrownBy(() -> jwtService.isValid(userDetails, token))
      .isInstanceOf(JwtException.class)
      .hasMessage("Invalid JWT token.");
  }
}
