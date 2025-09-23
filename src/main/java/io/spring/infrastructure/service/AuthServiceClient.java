package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.application.user.RegisterParam;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthServiceClient {
  private final RestTemplate restTemplate;
  private final String authServiceUrl;

  public AuthServiceClient(
      RestTemplate restTemplate, @Value("${auth.service.url}") String authServiceUrl) {
    this.restTemplate = restTemplate;
    this.authServiceUrl = authServiceUrl;
  }

  public Optional<ValidationResponse> validateToken(String token) {
    try {
      TokenValidationRequest request = new TokenValidationRequest(token);
      ResponseEntity<ValidationResponse> response =
          restTemplate.postForEntity(
              authServiceUrl + "/auth/validate", request, ValidationResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public UserWithTokenResponse register(RegisterRequest registerRequest) {
    try {
      ResponseEntity<UserWithTokenResponse> response =
          restTemplate.postForEntity(
              authServiceUrl + "/auth/register", registerRequest, UserWithTokenResponse.class);
      return response.getBody();
    } catch (Exception e) {
      throw new RuntimeException("Registration failed", e);
    }
  }

  public UserWithTokenResponse login(LoginRequest loginRequest) {
    try {
      ResponseEntity<UserWithTokenResponse> response =
          restTemplate.postForEntity(
              authServiceUrl + "/auth/login", loginRequest, UserWithTokenResponse.class);
      return response.getBody();
    } catch (Exception e) {
      throw new RuntimeException("Login failed", e);
    }
  }

  public Optional<Map> registerUser(RegisterParam registerParam) {
    try {
      ResponseEntity<Map> response =
          restTemplate.postForEntity(authServiceUrl + "/auth/register", registerParam, Map.class);
      return Optional.ofNullable(response.getBody());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<Map> loginUser(LoginParam loginParam) {
    try {
      ResponseEntity<Map> response =
          restTemplate.postForEntity(authServiceUrl + "/auth/login", loginParam, Map.class);
      return Optional.ofNullable(response.getBody());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TokenValidationRequest {
    private String token;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ValidationResponse {
    private boolean valid;
    private String userId;
    private String username;
  }

  @Data
  @JsonRootName("user")
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LoginParam {
    private String email;
    private String password;
  }

  @Data
  @JsonRootName("user")
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RegisterRequest {
    private String email;
    private String username;
    private String password;
  }

  @Data
  @JsonRootName("user")
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LoginRequest {
    private String email;
    private String password;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserWithTokenResponse {
    private UserData user;
    private String token;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserData {
    private String id;
    private String email;
    private String username;
    private String bio;
    private String image;
  }
}
