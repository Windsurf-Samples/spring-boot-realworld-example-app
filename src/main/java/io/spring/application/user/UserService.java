package io.spring.application.user;

import io.spring.core.user.User;
import io.spring.infrastructure.service.AuthServiceClient;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  @Autowired private AuthServiceClient authServiceClient;

  public User createUser(@Valid RegisterParam registerParam) {
    throw new UnsupportedOperationException("User creation moved to auth service");
  }

  public void updateUser(@Valid UpdateUserCommand command) {
    throw new UnsupportedOperationException("User updates moved to auth service");
  }
}
