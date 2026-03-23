package io.spring.infrastructure.repository;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.mapper.LocalUserCacheMapper;
import io.spring.infrastructure.service.auth.AuthServiceClient;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class RemoteUserRepository implements UserRepository {

  private final AuthServiceClient authServiceClient;
  private final LocalUserCacheMapper localUserCacheMapper;

  public RemoteUserRepository(
      AuthServiceClient authServiceClient, LocalUserCacheMapper localUserCacheMapper) {
    this.authServiceClient = authServiceClient;
    this.localUserCacheMapper = localUserCacheMapper;
  }

  @Override
  public void save(User user) {
    throw new UnsupportedOperationException(
        "User save operations should be performed via the auth-service directly");
  }

  @Override
  public Optional<User> findById(String id) {
    return authServiceClient
        .getCoreUserById(id)
        .map(
            data -> {
              User user = mapToUser(data);
              syncToLocalDb(user);
              return user;
            });
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return authServiceClient
        .getUserByUsername(username)
        .flatMap(userData -> authServiceClient.getCoreUserById(userData.getId()))
        .map(
            data -> {
              User user = mapToUser(data);
              syncToLocalDb(user);
              return user;
            });
  }

  @Override
  public Optional<User> findByEmail(String email) {
    throw new UnsupportedOperationException(
        "User email lookup should be performed via the auth-service directly");
  }

  @Override
  public void saveRelation(FollowRelation followRelation) {
    throw new UnsupportedOperationException(
        "Follow operations should be performed via the auth-service directly");
  }

  @Override
  public Optional<FollowRelation> findRelation(String userId, String targetId) {
    boolean isFollowing = authServiceClient.isUserFollowing(userId, targetId);
    if (isFollowing) {
      return Optional.of(new FollowRelation(userId, targetId));
    }
    return Optional.empty();
  }

  @Override
  public void removeRelation(FollowRelation followRelation) {
    throw new UnsupportedOperationException(
        "Unfollow operations should be performed via the auth-service directly");
  }

  private void syncToLocalDb(User user) {
    try {
      localUserCacheMapper.upsertUser(
          user.getId(), user.getUsername(), user.getEmail(), user.getBio(), user.getImage());
    } catch (Exception e) {
      // Best-effort cache sync; do not fail the request if local DB write fails
    }
  }

  private static User mapToUser(Map<String, String> data) {
    return new User(
        data.get("id"),
        data.get("email"),
        data.get("username"),
        "",
        data.get("bio"),
        data.get("image"));
  }
}
