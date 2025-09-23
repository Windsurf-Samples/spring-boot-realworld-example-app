package io.spring.core.user;

import java.util.Optional;

public interface UserRepository {
  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  Optional<User> findById(String id);

  void save(User user);

  void saveRelation(FollowRelation followRelation);

  Optional<FollowRelation> findRelation(String userId, String targetId);

  void removeRelation(FollowRelation followRelation);
}
