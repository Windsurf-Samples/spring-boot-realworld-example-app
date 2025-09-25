package io.spring.api;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
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
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    System.out.println(
        "DEBUG: GET /articles/"
            + slug
            + " - User: "
            + (user != null ? user.getUsername() : "anonymous"));
    var articleOpt = articleQueryService.findBySlug(slug, user);
    if (articleOpt.isPresent()) {
      System.out.println("DEBUG: GET /articles/" + slug + " - Article found");
      return ResponseEntity.ok(articleResponse(articleOpt.get()));
    } else {
      System.out.println("DEBUG: GET /articles/" + slug + " - Article not found");
      throw new ResourceNotFoundException();
    }
  }

  @PutMapping
  public ResponseEntity<?> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    System.out.println(
        "DEBUG: PUT /articles/"
            + slug
            + " - User: "
            + (user != null ? user.getUsername() : "anonymous"));
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              System.out.println(
                  "DEBUG: PUT /articles/" + slug + " - Article found, checking authorization");
              if (!AuthorizationService.canWriteArticle(user, article)) {
                System.out.println("DEBUG: PUT /articles/" + slug + " - Authorization failed");
                throw new NoAuthorizationException();
              }
              System.out.println(
                  "DEBUG: PUT /articles/" + slug + " - Authorization passed, updating article");
              Article updatedArticle =
                  articleCommandService.updateArticle(article, updateArticleParam);
              System.out.println(
                  "DEBUG: PUT /articles/" + slug + " - Article updated successfully");
              return ResponseEntity.ok(
                  articleResponse(
                      articleQueryService.findBySlug(updatedArticle.getSlug(), user).get()));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping
  public ResponseEntity deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    System.out.println(
        "DEBUG: DELETE /articles/"
            + slug
            + " - User: "
            + (user != null ? user.getUsername() : "anonymous"));
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              System.out.println(
                  "DEBUG: DELETE /articles/" + slug + " - Article found, checking authorization");
              if (!AuthorizationService.canWriteArticle(user, article)) {
                System.out.println("DEBUG: DELETE /articles/" + slug + " - Authorization failed");
                throw new NoAuthorizationException();
              }
              System.out.println(
                  "DEBUG: DELETE /articles/" + slug + " - Authorization passed, deleting article");
              articleRepository.remove(article);
              System.out.println(
                  "DEBUG: DELETE /articles/" + slug + " - Article deleted successfully");
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}
