package io.spring.core.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"userId", "targetId"})
public class FollowRelation {
  private String userId;
  private String targetId;

  public FollowRelation(String userId, String targetId) {
    this.userId = userId;
    this.targetId = targetId;
  }
}
