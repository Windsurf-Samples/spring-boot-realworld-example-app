package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
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
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;

  @Mock private CommentRepository commentRepository;

  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;

  private User user;

  @BeforeEach
  public void setUp() {
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
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
  public void should_create_comment_success() {
    String slug = "test-article";
    String body = "This is a test comment";
    Article article =
        new Article("Test Article", "Description", "Body", Arrays.asList("java"), "authorId");

    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), "", "", false);
    CommentData commentData =
        new CommentData(
            "commentId", body, article.getId(), new DateTime(), new DateTime(), profileData);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result = commentMutation.createComment(slug, body);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(commentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_throw_authentication_exception_when_not_logged_in_for_create() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.createComment("test-article", "comment"));
  }

  @Test
  public void should_throw_not_found_when_article_not_exists_for_create() {
    String slug = "nonexistent-article";
    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment(slug, "comment"));
  }

  @Test
  public void should_delete_comment_success() {
    String slug = "test-article";
    String commentId = "comment-id";
    Article article =
        new Article("Test Article", "Description", "Body", Arrays.asList("java"), user.getId());
    Comment comment = new Comment("comment body", user.getId(), article.getId());

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq(commentId)))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment(slug, commentId);

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(eq(comment));
  }

  @Test
  public void should_throw_authentication_exception_when_not_logged_in_for_delete() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.removeComment("test-article", "comment-id"));
  }

  @Test
  public void should_throw_not_found_when_article_not_exists_for_delete() {
    String slug = "nonexistent-article";
    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment(slug, "comment-id"));
  }

  @Test
  public void should_throw_not_found_when_comment_not_exists_for_delete() {
    String slug = "test-article";
    String commentId = "nonexistent-comment";
    Article article =
        new Article("Test Article", "Description", "Body", Arrays.asList("java"), user.getId());

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq(commentId)))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment(slug, commentId));
  }

  @Test
  public void should_throw_no_authorization_when_not_author_for_delete() {
    String slug = "test-article";
    String commentId = "comment-id";
    User anotherUser = new User("other@example.com", "otheruser", "password", "", "");
    Article article =
        new Article(
            "Test Article", "Description", "Body", Arrays.asList("java"), anotherUser.getId());
    Comment comment = new Comment("comment body", anotherUser.getId(), article.getId());

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq(commentId)))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class, () -> commentMutation.removeComment(slug, commentId));
  }
}
