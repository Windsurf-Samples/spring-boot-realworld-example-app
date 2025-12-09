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
import io.spring.application.data.ArticleEdge;
import io.spring.application.data.ArticlesConnection;
import io.spring.application.data.PageInfo;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.stream.Collectors;
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
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User user) {
    if (first != null || last != null) {
      return ResponseEntity.ok(getFeedWithCursor(user, first, last, after, before));
    }
    return ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
  }

  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User user) {
    if (first != null || last != null) {
      return ResponseEntity.ok(
          getArticlesWithCursor(tag, author, favoritedBy, user, first, last, after, before));
    }
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(
            tag, author, favoritedBy, new Page(offset, limit), user));
  }

  private ArticlesConnection getArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      User user,
      Integer first,
      Integer last,
      String after,
      String before) {
    CursorPager<ArticleData> articles;
    if (first != null) {
      articles =
          articleQueryService.findRecentArticlesWithCursor(
              tag,
              author,
              favoritedBy,
              new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT),
              user);
    } else {
      articles =
          articleQueryService.findRecentArticlesWithCursor(
              tag,
              author,
              favoritedBy,
              new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV),
              user);
    }
    return buildArticlesConnection(articles);
  }

  private ArticlesConnection getFeedWithCursor(
      User user, Integer first, Integer last, String after, String before) {
    CursorPager<ArticleData> articles;
    if (first != null) {
      articles =
          articleQueryService.findUserFeedWithCursor(
              user, new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT));
    } else {
      articles =
          articleQueryService.findUserFeedWithCursor(
              user, new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV));
    }
    return buildArticlesConnection(articles);
  }

  private ArticlesConnection buildArticlesConnection(CursorPager<ArticleData> articles) {
    PageInfo pageInfo =
        new PageInfo(
            articles.getStartCursor() != null ? articles.getStartCursor().toString() : null,
            articles.getEndCursor() != null ? articles.getEndCursor().toString() : null,
            articles.hasNext(),
            articles.hasPrevious());

    return new ArticlesConnection(
        articles.getData().stream()
            .map(a -> new ArticleEdge(a.getCursor().toString(), a))
            .collect(Collectors.toList()),
        pageInfo);
  }
}
