package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import io.spring.infrastructure.service.auth.AuthServiceClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@DgsComponent
@AllArgsConstructor
public class UserMutation {

  private UserRepository userRepository;
  private AuthServiceClient authServiceClient;
  private RestTemplate restTemplate;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateUser)
  public DataFetcherResult<UserResult> createUser(@InputArgument("input") CreateUserInput input) {
    Map<String, Object> userParam = new HashMap<>();
    userParam.put("email", input.getEmail());
    userParam.put("username", input.getUsername());
    userParam.put("password", input.getPassword());

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user", userParam);

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      restTemplate.postForEntity(
          authServiceClient.getAuthServiceUrl() + "/users", request, Map.class);
    } catch (HttpClientErrorException e) {
      return DataFetcherResult.<UserResult>newResult()
          .data(UserPayload.newBuilder().build())
          .build();
    }

    Optional<UserData> userData = authServiceClient.getUserByUsername(input.getUsername());
    if (userData.isPresent()) {
      User user =
          new User(
              userData.get().getId(),
              userData.get().getEmail(),
              userData.get().getUsername(),
              "",
              userData.get().getBio(),
              userData.get().getImage());
      return DataFetcherResult.<UserResult>newResult()
          .data(UserPayload.newBuilder().build())
          .localContext(user)
          .build();
    }

    return DataFetcherResult.<UserResult>newResult().data(UserPayload.newBuilder().build()).build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.Login)
  public DataFetcherResult<UserPayload> login(
      @InputArgument("password") String password, @InputArgument("email") String email) {
    Map<String, Object> userParam = new HashMap<>();
    userParam.put("email", email);
    userParam.put("password", password);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user", userParam);

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      ResponseEntity<Map> response =
          restTemplate.postForEntity(
              authServiceClient.getAuthServiceUrl() + "/users/login", request, Map.class);

      @SuppressWarnings("unchecked")
      Map<String, Object> userMap = (Map<String, Object>) response.getBody().get("user");
      String username = (String) userMap.get("username");

      Optional<UserData> userData = authServiceClient.getUserByUsername(username);
      if (userData.isPresent()) {
        User user =
            new User(
                userData.get().getId(),
                userData.get().getEmail(),
                userData.get().getUsername(),
                "",
                userData.get().getBio(),
                userData.get().getImage());
        return DataFetcherResult.<UserPayload>newResult()
            .data(UserPayload.newBuilder().build())
            .localContext(user)
            .build();
      }
    } catch (HttpClientErrorException e) {
      throw new InvalidAuthenticationException();
    }

    throw new InvalidAuthenticationException();
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

    Map<String, Object> userParam = new HashMap<>();
    userParam.put("email", updateUserInput.getEmail());
    userParam.put("username", updateUserInput.getUsername());
    userParam.put("bio", updateUserInput.getBio());
    userParam.put("password", updateUserInput.getPassword());
    userParam.put("image", updateUserInput.getImage());

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user", userParam);

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      restTemplate.put(authServiceClient.getAuthServiceUrl() + "/user", request);
    } catch (HttpClientErrorException e) {
      // Log error but continue
    }

    return DataFetcherResult.<UserPayload>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(currentUser)
        .build();
  }
}
