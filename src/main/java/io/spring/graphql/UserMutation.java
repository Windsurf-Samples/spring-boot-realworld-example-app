package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import io.spring.infrastructure.service.AuthServiceClient;
import io.spring.infrastructure.service.AuthServiceClient.LoginRequest;
import io.spring.infrastructure.service.AuthServiceClient.RegisterRequest;
import io.spring.infrastructure.service.AuthServiceClient.UserWithTokenResponse;
import javax.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@DgsComponent
@AllArgsConstructor
public class UserMutation {

  private UserRepository userRepository;
  private AuthServiceClient authServiceClient;
  private UserService userService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateUser)
  public DataFetcherResult<UserResult> createUser(@InputArgument("input") CreateUserInput input) {
    RegisterRequest registerRequest =
        new RegisterRequest(input.getEmail(), input.getUsername(), input.getPassword());
    try {
      UserWithTokenResponse response = authServiceClient.register(registerRequest);
      User user =
          new User(
              response.getUser().getEmail(),
              response.getUser().getUsername(),
              "",
              response.getUser().getBio(),
              response.getUser().getImage());
      user.setId(response.getUser().getId());
      return DataFetcherResult.<UserResult>newResult()
          .data(UserPayload.newBuilder().build())
          .localContext(user)
          .build();
    } catch (Exception e) {
      return DataFetcherResult.<UserResult>newResult()
          .data(
              GraphQLCustomizeExceptionHandler.getErrorsAsData(
                  new ConstraintViolationException("Registration failed", null)))
          .build();
    }
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.Login)
  public DataFetcherResult<UserPayload> login(
      @InputArgument("password") String password, @InputArgument("email") String email) {
    try {
      LoginRequest loginRequest = new LoginRequest(email, password);
      UserWithTokenResponse response = authServiceClient.login(loginRequest);
      User user =
          new User(
              response.getUser().getEmail(),
              response.getUser().getUsername(),
              "",
              response.getUser().getBio(),
              response.getUser().getImage());
      user.setId(response.getUser().getId());
      return DataFetcherResult.<UserPayload>newResult()
          .data(UserPayload.newBuilder().build())
          .localContext(user)
          .build();
    } catch (Exception e) {
      throw new InvalidAuthenticationException();
    }
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UpdateUser)
  public DataFetcherResult<UserPayload> updateUser(
      @InputArgument("changes") UpdateUserInput updateUserInput) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken
        || authentication.getPrincipal() == null) {
      return null;
    }
    io.spring.core.user.User currentUser = (io.spring.core.user.User) authentication.getPrincipal();
    UpdateUserParam param =
        UpdateUserParam.builder()
            .username(updateUserInput.getUsername())
            .email(updateUserInput.getEmail())
            .bio(updateUserInput.getBio())
            .password(updateUserInput.getPassword())
            .image(updateUserInput.getImage())
            .build();

    userService.updateUser(new UpdateUserCommand(currentUser, param));
    return DataFetcherResult.<UserPayload>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(currentUser)
        .build();
  }
}
