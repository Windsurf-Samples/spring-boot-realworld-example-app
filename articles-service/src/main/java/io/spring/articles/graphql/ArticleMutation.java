package io.spring.articles.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.articles.api.exception.ResourceNotFoundException;
import io.spring.articles.application.article.ArticleCommandService;
import io.spring.articles.application.article.NewArticleParam;
import io.spring.articles.application.article.UpdateArticleParam;
import io.spring.articles.core.article.Article;
import io.spring.articles.core.article.ArticleRepository;
import io.spring.articles.core.favorite.ArticleFavorite;
import io.spring.articles.core.favorite.ArticleFavoriteRepository;
import io.spring.articles.core.service.AuthorizationService;
import io.spring.articles.core.user.User;
import io.spring.articles.graphql.exception.AuthenticationException;
import io.spring.articles.graphql.types.ArticlePayload;
import io.spring.articles.graphql.types.CreateArticleInput;
import io.spring.articles.graphql.types.DeletionStatus;
import io.spring.articles.graphql.types.UpdateArticleInput;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class ArticleMutation {
  private ArticleCommandService articleCommandService;
  private ArticleRepository articleRepository;
  private ArticleFavoriteRepository articleFavoriteRepository;

  @DgsData(parentType = "Mutation", field = "createArticle")
  public DataFetcherResult<ArticlePayload> createArticle(
      @InputArgument("input") CreateArticleInput input) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    NewArticleParam newArticleParam =
        NewArticleParam.builder()
            .title(input.getTitle())
            .description(input.getDescription())
            .body(input.getBody())
            .tagList(input.getTagList())
            .build();
    Article article = articleCommandService.createArticle(newArticleParam, user);
    ArticlePayload articlePayload = ArticlePayload.newBuilder().build();
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(articlePayload)
        .localContext(article)
        .build();
  }

  @DgsData(parentType = "Mutation", field = "updateArticle")
  public DataFetcherResult<ArticlePayload> updateArticle(
      @InputArgument("slug") String slug, @InputArgument("changes") UpdateArticleInput input) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    if (!AuthorizationService.canWriteArticle(user, article)) {
      throw new ResourceNotFoundException();
    }
    UpdateArticleParam updateArticleParam =
        new UpdateArticleParam(input.getTitle(), input.getBody(), input.getDescription());
    article = articleCommandService.updateArticle(article, updateArticleParam);
    ArticlePayload articlePayload = ArticlePayload.newBuilder().build();
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(articlePayload)
        .localContext(article)
        .build();
  }

  @DgsData(parentType = "Mutation", field = "favoriteArticle")
  public DataFetcherResult<ArticlePayload> favoriteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
    articleFavoriteRepository.save(articleFavorite);
    ArticlePayload articlePayload = ArticlePayload.newBuilder().build();
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(articlePayload)
        .localContext(article)
        .build();
  }

  @DgsData(parentType = "Mutation", field = "unfavoriteArticle")
  public DataFetcherResult<ArticlePayload> unfavoriteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleFavoriteRepository
        .find(article.getId(), user.getId())
        .ifPresent(
            favorite -> {
              articleFavoriteRepository.remove(favorite);
            });
    ArticlePayload articlePayload = ArticlePayload.newBuilder().build();
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(articlePayload)
        .localContext(article)
        .build();
  }

  @DgsData(parentType = "Mutation", field = "deleteArticle")
  public DeletionStatus deleteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                throw new ResourceNotFoundException();
              }
              articleRepository.remove(article);
              return DeletionStatus.newBuilder().success(true).build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
