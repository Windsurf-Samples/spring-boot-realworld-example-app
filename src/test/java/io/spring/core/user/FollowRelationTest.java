package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user1", "user2");
    assertEquals("user1", relation.getUserId());
    assertEquals("user2", relation.getTargetId());
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    FollowRelation relation1 = new FollowRelation("user1", "user2");
    FollowRelation relation2 = new FollowRelation("user1", "user2");
    assertEquals(relation1, relation2);
  }

  @Test
  public void should_not_be_equal_with_different_target() {
    FollowRelation relation1 = new FollowRelation("user1", "user2");
    FollowRelation relation2 = new FollowRelation("user1", "user3");
    assertNotEquals(relation1, relation2);
  }

  @Test
  public void should_not_be_equal_with_different_user() {
    FollowRelation relation1 = new FollowRelation("user1", "user2");
    FollowRelation relation2 = new FollowRelation("user3", "user2");
    assertNotEquals(relation1, relation2);
  }
}
