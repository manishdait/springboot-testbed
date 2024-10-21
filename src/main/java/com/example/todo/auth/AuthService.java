package com.example.todo.auth;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.exception.TodoException;
import com.example.todo.jwt.JwtService;
import com.example.todo.shared.UuidGenerator;
import com.example.todo.user.User;
import com.example.todo.user.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  private final JwtService jwtService;
  private final UuidGenerator uuidGenerator;

  @Transactional
  public AuthResponseDto signUp(final AuthRequestDto request) {
    Optional<User> _user = userRepository.findByEmail(request.email());
    if (_user.isPresent()) {
      log.error("User with email=`{}` already exist", request.email());
      throw new TodoException("Duplicate Entry", String.format("User with email=`%s` already exist", request.email()));
    }

    User user = User.builder()
      .uuid(uuidGenerator.randomUUID())
      .email(request.email())
      .password(passwordEncoder.encode(request.password()))
      .build();
    
    userRepository.save(user);
    log.info("User created with email=`{}`", request.email());

    String authToken = jwtService.generateToken(request.email());
    return new AuthResponseDto(request.email(), authToken);
  }

  @Transactional
  public AuthResponseDto login(final AuthRequestDto request) {
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
      log.info("User authenticated for, username=`{}`, password=`{}`.", request.email(), request.password());
    } catch (BadCredentialsException e) {
      log.error("Invalid credentials for login, username=`{}`, password=`{}`.", request.email(), request.password());
      throw new BadCredentialsException("Invalid username or password");
    }

    String authToken = jwtService.generateToken(request.email());
    return new AuthResponseDto(request.email(), authToken);
  }

  public Optional<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication.getPrincipal() instanceof String) {
      log.info("Anonymous user session");
      return Optional.empty();
    }

    return Optional.of((User) authentication.getPrincipal());
  }
}
