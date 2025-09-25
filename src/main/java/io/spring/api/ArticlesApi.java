package io.spring.api;

import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles")
@AllArgsConstructor
public class ArticlesApi {
  private ArticleCommandService articleCommandService;
  private ArticleQueryService articleQueryService;

  @PostMapping
  public ResponseEntity createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal User user) {
    Article article = articleCommandService.createArticle(newArticleParam, user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleQueryService.findById(article.getId(), user).get());
          }
        });
  }

  @GetMapping(path = "feed")
  public ResponseEntity getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "last", required = false) Integer last,
      @AuthenticationPrincipal User user) {

    if (first != null || last != null || cursor != null) {
      if (first != null && last != null) {
        throw new IllegalArgumentException("Cannot specify both 'first' and 'last' parameters");
      }
      CursorPageParameter<DateTime> pageParam =
          new CursorPageParameter<>(
              DateTimeCursor.parse(cursor),
              first != null ? first : last,
              first != null ? Direction.NEXT : Direction.PREV);
      CursorPager<ArticleData> articles =
          articleQueryService.findUserFeedWithCursor(user, pageParam);
      return ResponseEntity.ok(buildCursorResponse(articles));
    }

    return ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
  }

  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal User user) {

    if (first != null || last != null || cursor != null) {
      if (first != null && last != null) {
        throw new IllegalArgumentException("Cannot specify both 'first' and 'last' parameters");
      }
      CursorPageParameter<DateTime> pageParam =
          new CursorPageParameter<>(
              DateTimeCursor.parse(cursor),
              first != null ? first : last,
              first != null ? Direction.NEXT : Direction.PREV);
      CursorPager<ArticleData> articles =
          articleQueryService.findRecentArticlesWithCursor(
              tag, author, favoritedBy, pageParam, user);
      return ResponseEntity.ok(buildCursorResponse(articles));
    }

    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(
            tag, author, favoritedBy, new Page(offset, limit), user));
  }

  private HashMap<String, Object> buildCursorResponse(CursorPager<ArticleData> articles) {
    return new HashMap<String, Object>() {
      {
        put("articles", articles.getData());
        put(
            "pageInfo",
            new HashMap<String, Object>() {
              {
                put("hasNextPage", articles.hasNext());
                put("hasPreviousPage", articles.hasPrevious());
                put(
                    "startCursor",
                    articles.getStartCursor() != null
                        ? articles.getStartCursor().toString()
                        : null);
                put(
                    "endCursor",
                    articles.getEndCursor() != null ? articles.getEndCursor().toString() : null);
              }
            });
        put(
            "edges",
            articles.getData().stream()
                .map(
                    article ->
                        new HashMap<String, Object>() {
                          {
                            put("cursor", article.getCursor().toString());
                            put("node", article);
                          }
                        })
                .collect(Collectors.toList()));
      }
    };
  }
}
