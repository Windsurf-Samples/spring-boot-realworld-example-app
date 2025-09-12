package io.spring.articles.api;

import io.spring.articles.api.exception.ResourceNotFoundException;
import io.spring.articles.application.ArticleQueryService;
import io.spring.articles.core.favorite.ArticleFavorite;
import io.spring.articles.core.favorite.ArticleFavoriteRepository;
import io.spring.articles.core.user.User;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleQueryService articleQueryService;

  @PostMapping
  public ResponseEntity favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .map(
            articleData -> {
              ArticleFavorite articleFavorite =
                  new ArticleFavorite(articleData.getId(), user.getId());
              articleFavoriteRepository.save(articleFavorite);
              return ResponseEntity.ok(
                  new HashMap<String, Object>() {
                    {
                      put("article", articleQueryService.findBySlug(slug, user).get());
                    }
                  });
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping
  public ResponseEntity unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .map(
            articleData -> {
              articleFavoriteRepository
                  .find(articleData.getId(), user.getId())
                  .ifPresent(
                      favorite -> {
                        articleFavoriteRepository.remove(favorite);
                      });
              return ResponseEntity.ok(
                  new HashMap<String, Object>() {
                    {
                      put("article", articleQueryService.findBySlug(slug, user).get());
                    }
                  });
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
