package io.spring.auth.api;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.auth.api.exception.InvalidAuthenticationException;
import io.spring.auth.application.user.RegisterParam;
import io.spring.auth.application.user.UserService;
import io.spring.auth.core.service.JwtService;
import io.spring.auth.core.user.User;
import io.spring.auth.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private UserService userService;

  @RequestMapping(path = "/register", method = POST)
  public ResponseEntity register(@Valid @RequestBody RegisterParam registerParam) {
    User user = userService.createUser(registerParam);
    return ResponseEntity.status(201)
        .body(userResponse(new UserWithToken(toUserData(user), jwtService.toToken(user))));
  }

  @RequestMapping(path = "/login", method = POST)
  public ResponseEntity login(@Valid @RequestBody LoginParam loginParam) {
    Optional<User> optional = userRepository.findByEmail(loginParam.getEmail());
    if (optional.isPresent()
        && passwordEncoder.matches(loginParam.getPassword(), optional.get().getPassword())) {
      User user = optional.get();
      return ResponseEntity.ok(
          userResponse(new UserWithToken(toUserData(user), jwtService.toToken(user))));
    } else {
      throw new InvalidAuthenticationException();
    }
  }

  @RequestMapping(path = "/validate", method = POST)
  public ResponseEntity validate(@Valid @RequestBody ValidateTokenParam validateTokenParam) {
    Optional<String> userIdOpt = jwtService.getSubFromToken(validateTokenParam.getToken());
    if (userIdOpt.isPresent()) {
      Optional<User> userOpt = userRepository.findById(userIdOpt.get());
      if (userOpt.isPresent()) {
        return ResponseEntity.ok(toUserData(userOpt.get()));
      }
    }
    return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
  }

  private UserData toUserData(User user) {
    return new UserData(
        user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
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

@Getter
@NoArgsConstructor
class ValidateTokenParam {
  @NotBlank(message = "can't be empty")
  private String token;
}

@Getter
@AllArgsConstructor
class UserData {
  private String id;
  private String email;
  private String username;
  private String bio;
  private String image;
}

@Getter
@AllArgsConstructor
class UserWithToken {
  private UserData user;
  private String token;
}
