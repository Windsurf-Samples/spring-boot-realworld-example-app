package io.spring.infrastructure.service.auth;

import io.spring.application.data.UserData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthServiceClient {

  private final RestTemplate restTemplate;
  private final String authServiceUrl;

  public AuthServiceClient(
      RestTemplate restTemplate,
      @Value("${auth-service.url:http://localhost:8081}") String authServiceUrl) {
    this.restTemplate = restTemplate;
    this.authServiceUrl = authServiceUrl;
  }

  public String getAuthServiceUrl() {
    return authServiceUrl;
  }

  public Optional<UserData> getUserById(String id) {
    try {
      ResponseEntity<UserData> response =
          restTemplate.getForEntity(authServiceUrl + "/api/internal/users/" + id, UserData.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<UserData> getUserByUsername(String username) {
    try {
      ResponseEntity<UserData> response =
          restTemplate.getForEntity(
              authServiceUrl + "/api/internal/users/by-username/" + username, UserData.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<UserData> verifyToken(String token) {
    try {
      Map<String, String> request = new HashMap<>();
      request.put("token", token);
      ResponseEntity<UserData> response =
          restTemplate.postForEntity(
              authServiceUrl + "/api/internal/users/verify-token", request, UserData.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      return Optional.empty();
    }
  }

  public Map<String, UserData> getUsersByIds(List<String> ids) {
    String idsParam = String.join(",", ids);
    ResponseEntity<Map<String, UserData>> response =
        restTemplate.exchange(
            authServiceUrl + "/api/internal/users/by-ids?ids=" + idsParam,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, UserData>>() {});
    return response.getBody() != null ? response.getBody() : new HashMap<>();
  }

  public boolean isUserFollowing(String userId, String targetId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              authServiceUrl + "/api/internal/users/" + userId + "/is-following/" + targetId,
              Boolean.class);
      return Boolean.TRUE.equals(response.getBody());
    } catch (Exception e) {
      return false;
    }
  }

  public List<String> getFollowedUsers(String userId) {
    ResponseEntity<List<String>> response =
        restTemplate.exchange(
            authServiceUrl + "/api/internal/users/" + userId + "/following",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<String>>() {});
    return response.getBody() != null ? response.getBody() : List.of();
  }

  public Set<String> getFollowingAuthors(String userId, List<String> ids) {
    Map<String, Object> request = new HashMap<>();
    request.put("userId", userId);
    request.put("ids", ids);
    ResponseEntity<Set<String>> response =
        restTemplate.exchange(
            authServiceUrl + "/api/internal/users/following-authors",
            HttpMethod.POST,
            new org.springframework.http.HttpEntity<>(request),
            new ParameterizedTypeReference<Set<String>>() {});
    return response.getBody() != null ? response.getBody() : Set.of();
  }

  public Optional<Map<String, String>> getCoreUserById(String id) {
    try {
      ResponseEntity<Map<String, String>> response =
          restTemplate.exchange(
              authServiceUrl + "/api/internal/users/" + id + "/core",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, String>>() {});
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
