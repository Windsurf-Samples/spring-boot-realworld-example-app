package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_all_fields() {
    Comment comment = new Comment("body content", "user123", "article456");
    assertEquals("body content", comment.getBody());
    assertEquals("user123", comment.getUserId());
    assertEquals("article456", comment.getArticleId());
    assertNotNull(comment.getId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_generate_unique_ids() {
    Comment comment1 = new Comment("body1", "user1", "article1");
    Comment comment2 = new Comment("body2", "user2", "article2");
    assertNotEquals(comment1.getId(), comment2.getId());
  }

  @Test
  public void should_have_equals_based_on_id() {
    Comment comment1 = new Comment("body", "user1", "article1");
    Comment comment2 = new Comment("body", "user1", "article1");
    assertNotEquals(comment1, comment2);
    assertEquals(comment1, comment1);
  }
}
