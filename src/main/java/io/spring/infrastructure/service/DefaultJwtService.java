package io.spring.infrastructure.service;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultJwtService implements JwtService {
  @Autowired(required = false)
  private AuthServiceClient authServiceClient;

  public DefaultJwtService() {}

  public DefaultJwtService(String secret, int sessionTime) {}

  @Override
  public String toToken(User user) {
    throw new UnsupportedOperationException("JWT generation moved to auth service");
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    if (authServiceClient == null) {
      return Optional.empty();
    }
    return authServiceClient
        .validateToken(token)
        .filter(response -> response.isValid())
        .map(response -> response.getUserId());
  }
}
