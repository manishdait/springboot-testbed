package com.example.todo.todo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.todo.auth.AuthService;
import com.example.todo.exception.TodoException;
import com.example.todo.shared.UuidGenerator;
import com.example.todo.user.User;
import com.example.todo.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
  @Mock
  private TodoRepository todoRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthService authService;

  @Mock
  private UuidGenerator uuidGenerator;

  @Mock
  private TodoDtoMapper todoDtoMapper;

  @Captor
  private ArgumentCaptor<Todo> todoCaptor;

  private TodoServcie todoServcie;

  @BeforeEach
  void setup() {
    this.todoServcie = new TodoServcie(todoRepository, userRepository, authService, uuidGenerator, todoDtoMapper);
  }

  @Test
  void returns_ListOfTodos_ForValidUser() {
    User user = new User(100L, "random-uuid", "encoded-password", "jhondoe@test.in");

    when(authService.getCurrentUser()).thenReturn(Optional.of(user));
    todoServcie.getTodos();

    verify(authService, times(1)).getCurrentUser();
    verify(todoRepository, times(1)).findByUser(user);
  }

  @Test
  void throws_TodoException_ForInvalidUser() {
    when(authService.getCurrentUser()).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> todoServcie.getTodos())
      .isInstanceOf(TodoException.class)
      .hasMessage("User is not authenticated");
  }

  @Test
  void return_TodoResponseDto_SuccessfullyCreatedNewTodo() {
    User user = new User(100L, "random-uuid", "encoded-password", "jhondoe@test.in");
    TodoRequestDto request = new TodoRequestDto("demo todo item in the list");
    String uuid = "random-uuid";

    Todo todo = Todo.builder()
      .title(request.title())
      .status(Status.PENDING)
      .uuid(uuid)
      .user(user)
      .build();

    when(authService.getCurrentUser()).thenReturn(Optional.of(user));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(uuidGenerator.randomUUID()).thenReturn(uuid);
    when(todoRepository.save(any(Todo.class))).thenReturn(todo);
    when(todoDtoMapper.todoToDto(todo)).thenReturn(new TodoResponseDto(uuid, request.title(), Status.PENDING, null, null));


    TodoResponseDto response = todoServcie.addTodo(request);

    verify(authService, times(1)).getCurrentUser();
    verify(userRepository, times(1)).findById(user.getId());


    Assertions.assertThat(response.uuid()).isEqualTo(uuid);
    Assertions.assertThat(response.title()).isEqualTo(request.title());
    Assertions.assertThat(response.status()).isEqualTo(Status.PENDING);
  }

  @Test
  void throws_TodoException_ForInvalidUser_DuringAdd() {
    TodoRequestDto request = new TodoRequestDto("demo todo item in the list");
    when(authService.getCurrentUser()).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> todoServcie.addTodo(request))
      .isInstanceOf(TodoException.class)
      .hasMessage("User is not authenticated");
  }

  @Test
  void returns_Todo_ForValidUuid() {
    String uuid = "random-uuid";

    Todo todo = Todo.builder()
      .id(101L)
      .title("demo todo item in the list")
      .status(Status.PENDING)
      .uuid("random-uuid")
      .user(new User())
      .build();
    
    when(todoRepository.findByUuid(uuid)).thenReturn(Optional.of(todo));
    when(todoDtoMapper.todoToDto(todo)).thenReturn(new TodoResponseDto(uuid, todo.getTitle(), todo.getStatus(), null, null));

    TodoResponseDto todoDto = todoServcie.getTodo(uuid);

    verify(todoRepository, times(1)).findByUuid(uuid);
    verify(todoDtoMapper, times(1)).todoToDto(todo);
    
    Assertions.assertThat(todoDto.uuid()).isEqualTo(uuid);
    Assertions.assertThat(todoDto.title()).isEqualTo(todo.getTitle());
    Assertions.assertThat(todoDto.status()).isEqualTo(todo.getStatus());
  }

  @Test
  void throws_TodoException_ForInvalidTodoId_WhileFetching() {
    when(todoRepository.findByUuid(anyString())).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> todoServcie.getTodo("random-uuid"))
      .isInstanceOf(TodoException.class)
      .hasMessage(String.format("Todo with uuid=`%s` does not exist", "random-uuid"));
  }

  @Test
  void return_UpdatedTodoResponseDto_ForStatusChange() {
    Todo todo = Todo.builder()
      .id(101L)
      .title("demo todo item in the list")
      .status(Status.PENDING)
      .uuid("random-uuid")
      .user(new User())
      .build();
    
    String uuid = "random-uuid";
    Status status = Status.COMPLETED;

    when(todoRepository.findByUuid(uuid)).thenReturn(Optional.of(todo));
    when(todoRepository.save(any(Todo.class))).thenReturn(todo);
    when(todoDtoMapper.todoToDto(todo)).thenReturn(new TodoResponseDto(uuid, todo.getTitle(), status, null, null));
    
    TodoResponseDto todoDto = todoServcie.update(uuid, Status.COMPLETED);

    verify(todoRepository, times(1)).findByUuid(uuid);
    verify(todoDtoMapper, times(1)).todoToDto(todo);
    
    Assertions.assertThat(todoDto.uuid()).isEqualTo(uuid);
    Assertions.assertThat(todoDto.title()).isEqualTo(todo.getTitle());
    Assertions.assertThat(todoDto.status()).isEqualTo(status);
  }

  @Test
  void throws_TodoException_ForInvalidTodoId_ForStatusChange() {
    when(todoRepository.findByUuid(anyString())).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> todoServcie.update("random-uuid", Status.COMPLETED))
      .isInstanceOf(TodoException.class)
      .hasMessage(String.format("Todo with uuid=`%s` does not exist", "random-uuid"));
  }

  @Test
  void return_UpdatedTodoResponseDto_ForTitleChange() {
    Todo todo = Todo.builder()
      .id(101L)
      .title("demo todo item in the list")
      .status(Status.PENDING)
      .uuid("random-uuid")
      .user(new User())
      .build();
    
    String uuid = "random-uuid";
    TodoRequestDto request = new TodoRequestDto("updated title todo");

    when(todoRepository.findByUuid(uuid)).thenReturn(Optional.of(todo));
    when(todoRepository.save(any(Todo.class))).thenReturn(todo);
    when(todoDtoMapper.todoToDto(todo)).thenReturn(new TodoResponseDto(uuid, request.title(), todo.getStatus(), null, null));
    
    TodoResponseDto todoDto = todoServcie.update(uuid, request);

    verify(todoRepository, times(1)).findByUuid(uuid);
    verify(todoDtoMapper, times(1)).todoToDto(todo);
    
    Assertions.assertThat(todoDto.uuid()).isEqualTo(uuid);
    Assertions.assertThat(todoDto.title()).isEqualTo(request.title());
    Assertions.assertThat(todoDto.status()).isEqualTo(todo.getStatus());
  }

  @Test
  void throws_TodoException_ForInvalidTodoId_ForTitleChange() {
    when(todoRepository.findByUuid(anyString())).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> todoServcie.update("random-uuid", new TodoRequestDto("updated title")))
      .isInstanceOf(TodoException.class)
      .hasMessage(String.format("Todo with uuid=`%s` does not exist", "random-uuid"));
  }

  @Test
  void delete_TodoById() {
    String uuid = "random-uuid";
    Todo todo = Todo.builder()
      .id(101L)
      .title("demo todo item in the list")
      .status(Status.PENDING)
      .uuid("random-uuid")
      .user(new User())
      .build();

    when(todoRepository.findByUuid(uuid)).thenReturn(Optional.of(todo));
    todoServcie.delete(uuid);
    verify(todoRepository).delete(todo);
  }

  @Test
  void throws_TodoException_ForInvalidId_WhenDelete() {
    when(todoRepository.findByUuid(anyString())).thenReturn(Optional.empty());
    Assertions.assertThatThrownBy(() -> todoServcie.delete("random-uuid"))
      .isInstanceOf(TodoException.class)
      .hasMessage(String.format("Todo with uuid=`%s` does not exist", "random-uuid"));;
  }
}
