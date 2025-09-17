package io.spring.application.user;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;

  public void updateUser(UpdateUserCommand command) {
    User user = command.getTargetUser();
    UpdateUserParam updateUserParam = command.getParam();

    String encodedPassword =
        updateUserParam.getPassword().isEmpty()
            ? ""
            : passwordEncoder.encode(updateUserParam.getPassword());

    user.update(
        updateUserParam.getEmail(),
        updateUserParam.getUsername(),
        encodedPassword,
        updateUserParam.getBio(),
        updateUserParam.getImage());

    userRepository.save(user);
  }
}
