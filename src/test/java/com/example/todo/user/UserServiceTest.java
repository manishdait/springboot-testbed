package com.example.todo.user;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.todo.TestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest extends TestUtils {
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @BeforeEach
  void setup() {
    this.userService = new UserService(this.userRepository);
  }

  @Test
  void return_Userdetails_ForValidEmail() {
    when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(VALID_USER));

    UserDetails _user = userService.loadUserByUsername(VALID_EMAIL);

    verify(userRepository, times(1)).findByEmail(VALID_EMAIL);
    Assertions.assertThat(_user).isEqualTo((UserDetails) VALID_USER);
  }

  @Test
  void throws_UsernotfoundException_ForInvalidEmail() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> userService.loadUserByUsername(INVALID_EMAIL))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessage(String.format("User with email=`%s` do not exist", INVALID_EMAIL));
  }
}
