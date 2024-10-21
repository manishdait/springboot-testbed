package com.example.todo.todo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.todo.user.User;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
  List<Todo> findByUser(User user);
  Optional<Todo> findByUuid(String uuid);
}
