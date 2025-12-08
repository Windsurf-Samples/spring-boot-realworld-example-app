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
import io.spring.application.data.ArticleDataListWithCursor;
import io.spring.core.article.Article;
import io.spring.core.user.User;
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
      @RequestParam(value = "offset", required = false) Integer offset,
      @RequestParam(value = "limit", required = false) Integer limit,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User user) {
    if (isCursorPaginationRequested(first, after, last, before)) {
      CursorPager<ArticleData> articles = getCursorPaginatedFeed(user, first, after, last, before);
      return ResponseEntity.ok(new ArticleDataListWithCursor(articles));
    }
    int effectiveOffset = offset != null ? offset : 0;
    int effectiveLimit = limit != null ? limit : 20;
    return ResponseEntity.ok(
        articleQueryService.findUserFeed(user, new Page(effectiveOffset, effectiveLimit)));
  }

  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", required = false) Integer offset,
      @RequestParam(value = "limit", required = false) Integer limit,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal User user) {
    if (isCursorPaginationRequested(first, after, last, before)) {
      CursorPager<ArticleData> articles =
          getCursorPaginatedArticles(tag, author, favoritedBy, user, first, after, last, before);
      return ResponseEntity.ok(new ArticleDataListWithCursor(articles));
    }
    int effectiveOffset = offset != null ? offset : 0;
    int effectiveLimit = limit != null ? limit : 20;
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(
            tag, author, favoritedBy, new Page(effectiveOffset, effectiveLimit), user));
  }

  private boolean isCursorPaginationRequested(
      Integer first, String after, Integer last, String before) {
    return first != null || after != null || last != null || before != null;
  }

  private CursorPager<ArticleData> getCursorPaginatedFeed(
      User user, Integer first, String after, Integer last, String before) {
    if (first != null) {
      return articleQueryService.findUserFeedWithCursor(
          user, new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT));
    } else if (last != null) {
      return articleQueryService.findUserFeedWithCursor(
          user, new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV));
    }
    return articleQueryService.findUserFeedWithCursor(
        user, new CursorPageParameter<>(null, 20, Direction.NEXT));
  }

  private CursorPager<ArticleData> getCursorPaginatedArticles(
      String tag,
      String author,
      String favoritedBy,
      User user,
      Integer first,
      String after,
      Integer last,
      String before) {
    if (first != null) {
      return articleQueryService.findRecentArticlesWithCursor(
          tag,
          author,
          favoritedBy,
          new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT),
          user);
    } else if (last != null) {
      return articleQueryService.findRecentArticlesWithCursor(
          tag,
          author,
          favoritedBy,
          new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV),
          user);
    }
    return articleQueryService.findRecentArticlesWithCursor(
        tag, author, favoritedBy, new CursorPageParameter<>(null, 20, Direction.NEXT), user);
  }
}
