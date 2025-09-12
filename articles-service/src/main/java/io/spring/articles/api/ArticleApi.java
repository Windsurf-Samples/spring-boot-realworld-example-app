package io.spring.articles.api;

import io.spring.articles.api.exception.ResourceNotFoundException;
import io.spring.articles.application.ArticleQueryService;
import io.spring.articles.application.article.ArticleCommandService;
import io.spring.articles.application.article.UpdateArticleParam;
import io.spring.articles.core.article.Article;
import io.spring.articles.core.article.ArticleRepository;
import io.spring.articles.core.service.AuthorizationService;
import io.spring.articles.core.user.User;
import java.util.HashMap;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  @GetMapping
  public ResponseEntity getArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .map(
            articleData -> {
              return ResponseEntity.ok(
                  new HashMap<String, Object>() {
                    {
                      put("article", articleData);
                    }
                  });
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping
  public ResponseEntity updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                throw new ResourceNotFoundException();
              }
              Article updatedArticle =
                  articleCommandService.updateArticle(article, updateArticleParam);
              return ResponseEntity.ok(
                  new HashMap<String, Object>() {
                    {
                      put(
                          "article",
                          articleQueryService.findBySlug(updatedArticle.getSlug(), user).get());
                    }
                  });
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping
  public ResponseEntity deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                throw new ResourceNotFoundException();
              }
              articleRepository.remove(article);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
