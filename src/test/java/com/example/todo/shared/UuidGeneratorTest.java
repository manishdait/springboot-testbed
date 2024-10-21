package com.example.todo.shared;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UuidGeneratorTest {
  private UuidGenerator uuidGenerator = new UuidGenerator();

  @Test
  void return_UuidAsString() {
    String uuid = uuidGenerator.randomUUID();
    Assertions.assertThat(uuid).isNotNull();
    Assertions.assertThat(uuid.length()).isGreaterThan(0);
  }
}
