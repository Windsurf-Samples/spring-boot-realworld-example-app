package io.spring.infrastructure.service.auth;

import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RemoteUserRelationshipQueryService implements UserRelationshipQueryService {

  private final AuthServiceClient authServiceClient;

  public RemoteUserRelationshipQueryService(AuthServiceClient authServiceClient) {
    this.authServiceClient = authServiceClient;
  }

  @Override
  public boolean isUserFollowing(String userId, String anotherUserId) {
    return authServiceClient.isUserFollowing(userId, anotherUserId);
  }

  @Override
  public Set<String> followingAuthors(String userId, List<String> ids) {
    return authServiceClient.getFollowingAuthors(userId, ids);
  }

  @Override
  public List<String> followedUsers(String userId) {
    return authServiceClient.getFollowedUsers(userId);
  }
}
