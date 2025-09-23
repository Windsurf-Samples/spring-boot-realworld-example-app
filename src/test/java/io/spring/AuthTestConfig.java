package io.spring;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.service.AuthServiceClient;
import java.util.Optional;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class AuthTestConfig {

  @Bean
  @Primary
  public JwtService jwtService() {
    return Mockito.mock(JwtService.class);
  }

  @Bean
  @Primary
  public UserRepository userRepository() {
    return Mockito.mock(UserRepository.class);
  }

  @Bean
  @Primary
  public UserReadService userReadService() {
    return Mockito.mock(UserReadService.class);
  }

  @Bean
  @Primary
  public AuthServiceClient authServiceClient() {
    AuthServiceClient mockClient = Mockito.mock(AuthServiceClient.class);
    
    try {
      when(mockClient.register(any())).thenAnswer(invocation -> {
        AuthServiceClient.RegisterRequest req = invocation.getArgument(0);
        AuthServiceClient.UserWithToken userWithToken = 
            new AuthServiceClient.UserWithToken("test-id", req.getEmail(), req.getUsername(), "", 
            "https://static.productionready.io/images/smiley-cyrus.jpg", "123");
        return new AuthServiceClient.UserWithTokenResponse(userWithToken);
      });
      
      when(mockClient.login(any())).thenAnswer(invocation -> {
        AuthServiceClient.LoginRequest req = invocation.getArgument(0);
        if ("123123".equals(req.getPassword())) {
          throw new RuntimeException("Invalid credentials");
        }
        String username = "john@jacob.com".equals(req.getEmail()) ? "johnjacob2" : "johnjacob";
        AuthServiceClient.UserWithToken userWithToken = 
            new AuthServiceClient.UserWithToken("test-id", req.getEmail(), username, "", 
            "https://static.productionready.io/images/smiley-cyrus.jpg", "123");
        return new AuthServiceClient.UserWithTokenResponse(userWithToken);
      });
      
      when(mockClient.validateToken(any())).thenAnswer(invocation -> {
        String token = invocation.getArgument(0);
        if ("token".equals(token) || "123".equals(token)) {
          return Optional.of(new AuthServiceClient.ValidationResponse(true, "test-user-id", "johnjacob"));
        }
        return Optional.empty();
      });
          
    } catch (Exception e) {
    }
    
    return mockClient;
  }

  @Bean
  @Primary
  public io.spring.application.user.UserService userService() {
    io.spring.application.user.UserService mockService = Mockito.mock(io.spring.application.user.UserService.class);
    return mockService;
  }
}
