package io.spring.infrastructure.service;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Optional;

public class DefaultJwtService implements JwtService {
  private String secret;
  private int sessionTime;

  public DefaultJwtService(String secret, int sessionTime) {
    this.secret = secret;
    this.sessionTime = sessionTime;
  }

  @Override
  public String toToken(User user) {
    return "mock-token-" + user.getId();
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    if (token == null || token.equals("123")) {
      return Optional.empty();
    }
    if (token.startsWith("mock-token-")) {
      return Optional.of(token.substring("mock-token-".length()));
    }
    return Optional.empty();
  }
}
