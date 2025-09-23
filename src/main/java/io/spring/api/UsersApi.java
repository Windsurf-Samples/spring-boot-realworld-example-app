package io.spring.api;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.infrastructure.service.AuthServiceClient;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UsersApi {
  private AuthServiceClient authServiceClient;

  @RequestMapping(path = "/users", method = POST)
  public ResponseEntity createUser(@Valid @RequestBody RegisterParam registerParam) {
    AuthServiceClient.RegisterRequest registerRequest =
        new AuthServiceClient.RegisterRequest(
            registerParam.getEmail(), registerParam.getUsername(), registerParam.getPassword());
    try {
      AuthServiceClient.UserWithTokenResponse response =
          authServiceClient.register(registerRequest);
      return ResponseEntity.status(201).body(response);
    } catch (Exception e) {
      throw new InvalidAuthenticationException();
    }
  }

  @RequestMapping(path = "/users/login", method = POST)
  public ResponseEntity userLogin(@Valid @RequestBody LoginParam loginParam) {
    AuthServiceClient.LoginRequest loginRequest =
        new AuthServiceClient.LoginRequest(loginParam.getEmail(), loginParam.getPassword());
    try {
      AuthServiceClient.UserWithTokenResponse response = authServiceClient.login(loginRequest);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new InvalidAuthenticationException();
    }
  }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class LoginParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;

  @NotBlank(message = "can't be empty")
  private String password;
}
