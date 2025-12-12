package io.spring.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User articleOwner;
  private User commentOwner;
  private User otherUser;
  private Article article;
  private Comment comment;

  @BeforeEach
  public void setUp() {
    articleOwner = new User("owner@example.com", "owner", "password", "bio", "image");
    commentOwner = new User("commenter@example.com", "commenter", "password", "bio", "image");
    otherUser = new User("other@example.com", "other", "password", "bio", "image");

    article =
        new Article(
            "Test Article", "description", "body", Arrays.asList("java"), articleOwner.getId());
    comment = new Comment("Test comment", commentOwner.getId(), article.getId());
  }

  @Test
  public void should_allow_article_owner_to_write_article() {
    boolean canWrite = AuthorizationService.canWriteArticle(articleOwner, article);

    assertThat(canWrite, is(true));
  }

  @Test
  public void should_not_allow_non_owner_to_write_article() {
    boolean canWrite = AuthorizationService.canWriteArticle(otherUser, article);

    assertThat(canWrite, is(false));
  }

  @Test
  public void should_allow_article_owner_to_write_comment() {
    boolean canWrite = AuthorizationService.canWriteComment(articleOwner, article, comment);

    assertThat(canWrite, is(true));
  }

  @Test
  public void should_allow_comment_owner_to_write_comment() {
    boolean canWrite = AuthorizationService.canWriteComment(commentOwner, article, comment);

    assertThat(canWrite, is(true));
  }

  @Test
  public void should_not_allow_other_user_to_write_comment() {
    boolean canWrite = AuthorizationService.canWriteComment(otherUser, article, comment);

    assertThat(canWrite, is(false));
  }

  @Test
  public void should_allow_user_who_is_both_article_and_comment_owner() {
    Comment ownerComment = new Comment("Owner comment", articleOwner.getId(), article.getId());

    boolean canWrite = AuthorizationService.canWriteComment(articleOwner, article, ownerComment);

    assertThat(canWrite, is(true));
  }
}
