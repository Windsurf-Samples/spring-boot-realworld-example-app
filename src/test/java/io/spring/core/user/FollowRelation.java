package io.spring.core.user;

public class FollowRelation {
  private String userId;
  private String targetId;

  public FollowRelation(String userId, String targetId) {
    this.userId = userId;
    this.targetId = targetId;
  }

  public String getUserId() { return userId; }
  public String getTargetId() { return targetId; }
}
