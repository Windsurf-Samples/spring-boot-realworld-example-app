package io.spring.articles.api;

import io.spring.articles.application.ArticleQueryService;
import io.spring.articles.application.Page;
import io.spring.articles.application.article.ArticleCommandService;
import io.spring.articles.application.article.NewArticleParam;
import io.spring.articles.core.user.User;
import java.util.HashMap;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/articles")
@AllArgsConstructor
public class ArticlesApi {
  private ArticleQueryService articleQueryService;
  private ArticleCommandService articleCommandService;

  @PostMapping
  public ResponseEntity createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleCommandService.createArticle(newArticleParam, user));
          }
        });
  }

  @GetMapping(path = "feed")
  public ResponseEntity getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
  }

  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "author", required = false) String author,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(
            tag, author, favoritedBy, new Page(offset, limit), user));
  }
}
