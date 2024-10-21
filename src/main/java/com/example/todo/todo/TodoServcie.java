package com.example.todo.todo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.auth.AuthService;
import com.example.todo.exception.TodoException;
import com.example.todo.user.User;
import com.example.todo.user.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class TodoServcie {
  private final TodoRepository todoRepository;
  private final UserRepository userRepository;

  private final AuthService authService;

  @Transactional
  public List<TodoResponseDto> getTodos() {
    Optional<User> user = authService.getCurrentUser();
    if (user.isEmpty()) {
      log.error("User is not authenticated");
      throw new TodoException("Unauthorize Session", "User is not authenticated");
    }

    log.info("Fetching todos for email=`{}`", user.get().getEmail());
    return todoRepository.findByUser(user.get()).stream().map(todo -> todoToDto(todo)).toList();
  }

  @Transactional
  public TodoResponseDto addTodo(TodoRequestDto request) {
    Long id = authService.getCurrentUser().orElseThrow(() -> new TodoException("Unauthorize Session", "User is not authenticated")).getId();
    User user = userRepository.findById(id).get();

    Todo todo = Todo.builder()
      .uuid(UUID.randomUUID().toString())
      .title(request.title())
      .status(Status.PENDING)
      .user(user)
      .build();

    log.info("Added todo with id=`{}`", todo.getUuid());

    return todoToDto(todoRepository.save(todo));
  }

  @Transactional
  public TodoResponseDto getTodo(String id) {
    Todo todo = todoRepository.findByUuid(id).orElseThrow(() -> new TodoException("Todo Not Found", String.format("Todo with uuid=`%s` does not exist", id)));
    log.info("Fetch todo with id=`{}`", id);
    return todoToDto(todo);
  }

  @Transactional
  public TodoResponseDto update(String id, Status status) {
    Todo todo = todoRepository.findByUuid(id).orElseThrow(() -> new TodoException("Todo Not Found", String.format("Todo with uuid=`%s` does not exist", id)));
    todo.setStatus(status);
    log.info("Change status for todo with id=`{}`", id);
    return todoToDto(todoRepository.save(todo));
  }

  @Transactional
  public TodoResponseDto update(String id, TodoRequestDto request) {
    Todo todo = todoRepository.findByUuid(id).orElseThrow(() -> new TodoException("Todo Not Found", String.format("Todo with uuid=`%s` does not exist", id)));
    todo.setTitle(request.title());
    log.info("Updated title for todo with id=`{}`", id);
    return todoToDto(todoRepository.save(todo));
  }

  @Transactional
  public void delete(String id) {
    Todo todo = todoRepository.findByUuid(id).orElseThrow(() -> new TodoException("Todo Not Found", String.format("Todo with uuid=`%s` does not exist", id)));
    log.info("Deleting todo with id=`{}`", id);
    todoRepository.delete(todo);
  }

  private TodoResponseDto todoToDto(Todo todo) {
    return new TodoResponseDto(todo.getUuid(), todo.getTitle(), todo.getStatus(), todo.getCreatedAt(), todo.getUpdatedAt());
  }
}
