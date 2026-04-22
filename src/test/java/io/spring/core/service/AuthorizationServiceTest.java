package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void should_allow_author_to_write_article() {
    User user = new User("test@email.com", "user", "pass", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  public void should_deny_non_author_to_write_article() {
    User author = new User("author@email.com", "author", "pass", "", "");
    User other = new User("other@email.com", "other", "pass", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), author.getId());
    assertFalse(AuthorizationService.canWriteArticle(other, article));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User articleAuthor = new User("author@email.com", "author", "pass", "", "");
    User commenter = new User("commenter@email.com", "commenter", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User articleAuthor = new User("author@email.com", "author", "pass", "", "");
    User commenter = new User("commenter@email.com", "commenter", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(commenter, article, comment));
  }

  @Test
  public void should_deny_unrelated_user_to_write_comment() {
    User articleAuthor = new User("author@email.com", "author", "pass", "", "");
    User commenter = new User("commenter@email.com", "commenter", "pass", "", "");
    User other = new User("other@email.com", "other", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());
    assertFalse(AuthorizationService.canWriteComment(other, article, comment));
  }
}
