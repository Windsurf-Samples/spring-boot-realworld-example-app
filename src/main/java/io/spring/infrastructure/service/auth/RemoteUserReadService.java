package io.spring.infrastructure.service.auth;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import org.springframework.stereotype.Service;

@Service
public class RemoteUserReadService implements UserReadService {

  private final AuthServiceClient authServiceClient;

  public RemoteUserReadService(AuthServiceClient authServiceClient) {
    this.authServiceClient = authServiceClient;
  }

  @Override
  public UserData findByUsername(String username) {
    return authServiceClient.getUserByUsername(username).orElse(null);
  }

  @Override
  public UserData findById(String id) {
    return authServiceClient.getUserById(id).orElse(null);
  }
}
