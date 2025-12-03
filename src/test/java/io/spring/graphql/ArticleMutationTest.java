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
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
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
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;

  @Mock private ArticleFavoriteRepository articleFavoriteRepository;

  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;

  private User user;

  @BeforeEach
  public void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
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
  public void should_create_article_success() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article =
        new Article(
            "Test Article",
            "Test Description",
            "Test Body",
            Arrays.asList("java", "spring"),
            user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  public void should_throw_authentication_exception_when_not_logged_in_for_create() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  public void should_update_article_success() {
    String slug = "test-article";
    Article article =
        new Article(
            "Test Article", "Old Description", "Old Body", Arrays.asList("java"), user.getId());

    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .description("Updated Description")
            .body("Updated Body")
            .build();

    Article updatedArticle =
        new Article(
            "Updated Title",
            "Updated Description",
            "Updated Body",
            Arrays.asList("java"),
            user.getId());

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(updatedArticle);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle(slug, input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(updatedArticle, result.getLocalContext());
  }

  @Test
  public void should_throw_not_found_when_article_not_exists_for_update() {
    String slug = "nonexistent-article";
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.updateArticle(slug, input));
  }

  @Test
  public void should_throw_no_authorization_when_not_author_for_update() {
    String slug = "test-article";
    User anotherUser = new User("other@example.com", "otheruser", "password", "", "");
    Article article =
        new Article(
            "Test Article", "Description", "Body", Arrays.asList("java"), anotherUser.getId());

    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.updateArticle(slug, input));
  }

  @Test
  public void should_favorite_article_success() {
    String slug = "test-article";
    Article article =
        new Article("Test Article", "Description", "Body", Arrays.asList("java"), "someUserId");

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(slug);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_unfavorite_article_success() {
    String slug = "test-article";
    Article article =
        new Article("Test Article", "Description", "Body", Arrays.asList("java"), "someUserId");
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(slug);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleFavoriteRepository).remove(eq(favorite));
  }

  @Test
  public void should_delete_article_success() {
    String slug = "test-article";
    Article article =
        new Article("Test Article", "Description", "Body", Arrays.asList("java"), user.getId());

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(slug);

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  public void should_throw_no_authorization_when_not_author_for_delete() {
    String slug = "test-article";
    User anotherUser = new User("other@example.com", "otheruser", "password", "", "");
    Article article =
        new Article(
            "Test Article", "Description", "Body", Arrays.asList("java"), anotherUser.getId());

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle(slug));
  }
}
