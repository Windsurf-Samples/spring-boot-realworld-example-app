package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder encryptService;

  @Mock private UserService userService;

  private UserMutation userMutation;

  @BeforeEach
  public void setUp() {
    userMutation = new UserMutation(userRepository, encryptService, userService);
  }

  @Test
  public void should_create_user_success() {
    String email = "test@example.com";
    String username = "testuser";
    String password = "password123";

    CreateUserInput input =
        CreateUserInput.newBuilder().email(email).username(username).password(password).build();

    User user = new User(email, username, password, "", "");
    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_login_success() {
    String email = "test@example.com";
    String username = "testuser";
    String password = "password123";
    String encodedPassword = "encodedPassword";

    User user = new User(email, username, encodedPassword, "", "");
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq(password), eq(encodedPassword))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login(password, email);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_fail_login_with_wrong_password() {
    String email = "test@example.com";
    String username = "testuser";
    String password = "password123";
    String wrongPassword = "wrongpassword";
    String encodedPassword = "encodedPassword";

    User user = new User(email, username, encodedPassword, "", "");
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq(wrongPassword), eq(encodedPassword))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login(wrongPassword, email));
  }

  @Test
  public void should_fail_login_with_nonexistent_email() {
    String email = "nonexistent@example.com";
    String password = "password123";

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    assertThrows(InvalidAuthenticationException.class, () -> userMutation.login(password, email));
  }
}
