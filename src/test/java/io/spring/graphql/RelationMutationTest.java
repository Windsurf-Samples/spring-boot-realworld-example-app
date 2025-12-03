package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;

  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;

  private User user;

  @BeforeEach
  public void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    user = new User("test@example.com", "testuser", "password", "", "");
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_follow_user_success() {
    String targetUsername = "targetuser";
    User targetUser = new User("target@example.com", targetUsername, "password", "bio", "image");
    ProfileData profileData =
        new ProfileData(targetUser.getId(), targetUsername, "bio", "image", true);

    when(userRepository.findByUsername(eq(targetUsername))).thenReturn(Optional.of(targetUser));
    when(profileQueryService.findByUsername(eq(targetUsername), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow(targetUsername);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals(targetUsername, result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  public void should_throw_authentication_exception_when_not_logged_in_for_follow() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  public void should_throw_not_found_when_user_not_exists_for_follow() {
    String targetUsername = "nonexistent";
    when(userRepository.findByUsername(eq(targetUsername))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow(targetUsername));
  }

  @Test
  public void should_unfollow_user_success() {
    String targetUsername = "targetuser";
    User targetUser = new User("target@example.com", targetUsername, "password", "bio", "image");
    FollowRelation followRelation = new FollowRelation(user.getId(), targetUser.getId());
    ProfileData profileData =
        new ProfileData(targetUser.getId(), targetUsername, "bio", "image", false);

    when(userRepository.findByUsername(eq(targetUsername))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(user.getId()), eq(targetUser.getId())))
        .thenReturn(Optional.of(followRelation));
    when(profileQueryService.findByUsername(eq(targetUsername), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow(targetUsername);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals(targetUsername, result.getProfile().getUsername());
    verify(userRepository).removeRelation(eq(followRelation));
  }

  @Test
  public void should_throw_authentication_exception_when_not_logged_in_for_unfollow() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  public void should_throw_not_found_when_user_not_exists_for_unfollow() {
    String targetUsername = "nonexistent";
    when(userRepository.findByUsername(eq(targetUsername))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow(targetUsername));
  }

  @Test
  public void should_throw_not_found_when_relation_not_exists_for_unfollow() {
    String targetUsername = "targetuser";
    User targetUser = new User("target@example.com", targetUsername, "password", "bio", "image");

    when(userRepository.findByUsername(eq(targetUsername))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(user.getId()), eq(targetUser.getId())))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow(targetUsername));
  }
}
