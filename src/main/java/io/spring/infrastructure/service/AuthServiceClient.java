package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.Optional;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthServiceClient {
  private final RestTemplate restTemplate;
  private final String authServiceUrl;

  public AuthServiceClient(@Value("${auth.service.url}") String authServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.authServiceUrl = authServiceUrl;
  }

  public Optional<UserInfo> validateToken(String token) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      String requestBody = "{\"ValidateTokenParam\": {\"token\": \"" + token + "\"}}";
      System.out.println(
          "AuthServiceClient: Sending request to " + authServiceUrl + "/auth/validate");
      System.out.println("AuthServiceClient: Request body: " + requestBody);
      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

      ResponseEntity<UserInfo> response =
          restTemplate.postForEntity(authServiceUrl + "/auth/validate", entity, UserInfo.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        System.out.println("AuthServiceClient: Validation successful");
        return Optional.of(response.getBody());
      }
    } catch (Exception e) {
      System.out.println("AuthServiceClient: Exception during validation: " + e.getMessage());
    }
    return Optional.empty();
  }

  @Getter
  @JsonRootName("ValidateTokenParam")
  public static class ValidateTokenRequest {
    private final String token;

    public ValidateTokenRequest(String token) {
      this.token = token;
    }
  }

  @Getter
  public static class UserInfo {
    @JsonProperty("id")
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("image")
    private String image;
  }
}
