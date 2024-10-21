package com.example.todo.todo;

import com.example.todo.shared.AuditEntity;
import com.example.todo.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "todo_list")
@Entity
public class Todo extends AuditEntity {
  @JsonIgnore
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "todo_generator")
  @SequenceGenerator(name = "todo_generator", sequenceName = "todo_generator_sequence", initialValue = 101)
  private Long id;

  @NotBlank(message = "uuid cannot be blank")
  private String uuid;

  @NotBlank(message = "title cannot be blank")
  private String title;

  @NonNull
  @Enumerated(value = EnumType.STRING)
  private Status status;

  @JsonIgnore
  @JoinColumn(name = "app_user_id")
  @ManyToOne(cascade = CascadeType.ALL)
  private User user;
}
