package io.spring.articles.core.user;

import java.util.Optional;

public interface UserRepository {
  void save(User user);

  Optional<User> findById(String id);

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);
}
