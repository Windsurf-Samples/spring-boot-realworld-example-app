package io.spring.auth.api.internal;

import io.spring.auth.application.data.UserData;
import io.spring.auth.core.service.JwtService;
import io.spring.auth.core.user.User;
import io.spring.auth.core.user.UserRepository;
import io.spring.auth.infrastructure.mybatis.readservice.UserReadService;
import io.spring.auth.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@AllArgsConstructor
public class InternalUserApi {

  private UserRepository userRepository;
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private JwtService jwtService;

  @GetMapping("/users/{id}")
  public ResponseEntity<UserData> getUserById(@PathVariable("id") String id) {
    UserData userData = userReadService.findById(id);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/users/by-username/{username}")
  public ResponseEntity<UserData> getUserByUsername(@PathVariable("username") String username) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userData);
  }

  @PostMapping("/users/verify-token")
  public ResponseEntity<UserData> verifyToken(@RequestBody Map<String, String> request) {
    String token = request.get("token");
    if (token == null) {
      return ResponseEntity.badRequest().build();
    }
    Optional<String> userIdOpt = jwtService.getSubFromToken(token);
    if (userIdOpt.isEmpty()) {
      return ResponseEntity.status(401).build();
    }
    UserData userData = userReadService.findById(userIdOpt.get());
    if (userData == null) {
      return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(userData);
  }

  @GetMapping("/users/by-ids")
  public ResponseEntity<Map<String, UserData>> getUsersByIds(
      @RequestParam("ids") List<String> ids) {
    Map<String, UserData> result = new HashMap<>();
    for (String id : ids) {
      UserData userData = userReadService.findById(id);
      if (userData != null) {
        result.put(id, userData);
      }
    }
    return ResponseEntity.ok(result);
  }

  @GetMapping("/users/{userId}/following")
  public ResponseEntity<List<String>> getFollowedUsers(@PathVariable("userId") String userId) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
    return ResponseEntity.ok(followedUsers);
  }

  @GetMapping("/users/{userId}/is-following/{targetId}")
  public ResponseEntity<Boolean> isUserFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetId") String targetId) {
    boolean isFollowing = userRelationshipQueryService.isUserFollowing(userId, targetId);
    return ResponseEntity.ok(isFollowing);
  }

  @PostMapping("/users/following-authors")
  public ResponseEntity<Set<String>> getFollowingAuthors(@RequestBody Map<String, Object> request) {
    String userId = (String) request.get("userId");
    @SuppressWarnings("unchecked")
    List<String> ids = (List<String>) request.get("ids");
    Set<String> followingAuthors = userRelationshipQueryService.followingAuthors(userId, ids);
    return ResponseEntity.ok(followingAuthors);
  }

  @GetMapping("/users/{id}/core")
  public ResponseEntity<Map<String, String>> getCoreUserById(@PathVariable("id") String id) {
    Optional<User> userOpt = userRepository.findById(id);
    if (userOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    User user = userOpt.get();
    Map<String, String> result = new HashMap<>();
    result.put("id", user.getId());
    result.put("email", user.getEmail());
    result.put("username", user.getUsername());
    result.put("bio", user.getBio());
    result.put("image", user.getImage());
    return ResponseEntity.ok(result);
  }
}
