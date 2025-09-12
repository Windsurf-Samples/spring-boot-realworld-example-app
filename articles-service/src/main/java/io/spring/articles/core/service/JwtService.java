package io.spring.articles.core.service;

import io.spring.articles.core.user.User;
import java.util.Optional;

public interface JwtService {
  String toToken(User user);

  Optional<String> getSubFromToken(String token);
}
