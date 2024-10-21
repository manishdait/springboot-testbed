package com.example.todo.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.todo.exception.TodoException;
import com.example.todo.jwt.JwtService;
import com.example.todo.shared.UuidGenerator;
import com.example.todo.user.User;
import com.example.todo.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtService jwtService;

  @Mock
  private UuidGenerator uuidGenerator;

  private AuthService authService;

  @Captor
  ArgumentCaptor<User> userCaptor;

  @BeforeEach
  void setup() {
    this.authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService, uuidGenerator);
  }

  @Test
  void return_AuthResponseDto_ForSuccessfullCreationOfUser() {
    String encodedPassword = "encodedPassword";
    String uuid = "123e4567-e89b-12d3-a456-426614174000";
    AuthResponseDto expected = new AuthResponseDto("jhondoe@test.in", "authToken");

    //given
    AuthRequestDto request = new AuthRequestDto("jhondoe@test.in", "Jhon@24");

    //when
    when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
    when(uuidGenerator.randomUUID()).thenReturn(uuid);
    when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
    when(jwtService.generateToken(request.email())).thenReturn("authToken");

    AuthResponseDto response = authService.signUp(request);

    //then
    verify(userRepository, times(1)).findByEmail(request.email());
    verify(uuidGenerator, times(1)).randomUUID();
    verify(passwordEncoder, times(1)).encode(request.password());
    verify(jwtService, times(1)).generateToken(request.email());

    verify(userRepository).save(userCaptor.capture());
    User user = userCaptor.getValue();

    Assertions.assertThat(user.getEmail()).isEqualTo(request.email());
    Assertions.assertThat(user.getPassword()).isEqualTo(encodedPassword);
    Assertions.assertThat(user.getUuid()).isEqualTo(uuid);

    Assertions.assertThat(response).isEqualTo(expected);
  }

  @Test
  void thows_TodoException_ForDuplicateUser() {
    // given
    AuthRequestDto request = new AuthRequestDto("jhondoe@test.in", "Jhon@24");

    //when
    when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

    //then
    Assertions.assertThatThrownBy(() -> authService.signUp(request))
      .isInstanceOf(TodoException.class)
      .hasMessage(String.format("User with email=`%s` already exist", request.email()));
  }

  @Test
  void return_AuthResponseDto_ForSuccessfulLogin() {
    AuthResponseDto expected = new AuthResponseDto("jhondoe@test.in", "authToken");
    Authentication authentication = new UsernamePasswordAuthenticationToken("jhondoe@test.in", "Jhon@24");

    //given
    AuthRequestDto request = new AuthRequestDto("jhondoe@test.in", "Jhon@24");

    //when
    when(authenticationManager.authenticate(authentication)).thenReturn(authentication);
    when(jwtService.generateToken(request.email())).thenReturn("authToken");

    AuthResponseDto response = authService.login(request);

    //then
    verify(authenticationManager, times(1)).authenticate(authentication);
    verify(jwtService, times(1)).generateToken(request.email());

    Assertions.assertThat(response).isEqualTo(expected);
  }

  @Test
  void throw_BadCredentialException_ForUnsuccessfulLogin() {
    //given
    AuthRequestDto request = new AuthRequestDto("katedoe@test.in", "Jhon@24");

    //when
    when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Invalid username or password"));
    
    Assertions.assertThatThrownBy(() -> authService.login(request))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("Invalid username or password");
  }

  @Test
  void return_UserOptional_IfUserLogin() {
    User user = new User(100L, "random-uuid", "encoded-password", "jhondoe@test.in");
    Authentication authentication = Mockito.mock(Authentication.class);
    Mockito.when(authentication.getPrincipal()).thenReturn(user);

    SecurityContextHolder.getContext().setAuthentication(authentication);
    Optional<User> currentUser = authService.getCurrentUser();

    Assertions.assertThat(currentUser).isPresent();
  }

  @Test
  void return_EmptyOptional_IfNoUserLogin() {
    Authentication authentication = Mockito.mock(Authentication.class);
    Mockito.when(authentication.getPrincipal()).thenReturn("anonymousUser");

    SecurityContextHolder.getContext().setAuthentication(authentication);
    Optional<User> currentUser = authService.getCurrentUser();

    Assertions.assertThat(currentUser).isEmpty();
  }
}
