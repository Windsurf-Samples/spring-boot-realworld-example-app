package io.spring.api;

import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.data.ArticleCursorList;
import io.spring.application.data.PageInfo;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.HashMap;
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
      @RequestParam(value = "offset", required = false) Integer offset,
      @RequestParam(value = "limit", required = false) Integer limit,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User user) {

    if (first != null || last != null) {
      if (first == null && last == null) {
        throw new IllegalArgumentException(
            "Either first or last must be provided for cursor pagination");
      }
      CursorPageParameter<DateTime> pageParam;
      if (first != null) {
        pageParam = new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT);
      } else {
        pageParam = new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV);
      }
      CursorPager<io.spring.application.data.ArticleData> result =
          articleQueryService.findUserFeedWithCursor(user, pageParam);
      PageInfo pageInfo =
          new PageInfo(
              result.hasNext(),
              result.hasPrevious(),
              result.getStartCursor() == null ? null : result.getStartCursor().toString(),
              result.getEndCursor() == null ? null : result.getEndCursor().toString());
      return ResponseEntity.ok(new ArticleCursorList(result.getData(), pageInfo));
    } else {
      int offsetVal = offset != null ? offset : 0;
      int limitVal = limit != null ? limit : 20;
      return ResponseEntity.ok(
          articleQueryService.findUserFeed(
              user, new io.spring.application.Page(offsetVal, limitVal)));
    }
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

    if (first != null || last != null) {
      if (first == null && last == null) {
        throw new IllegalArgumentException(
            "Either first or last must be provided for cursor pagination");
      }
      CursorPageParameter<DateTime> pageParam;
      if (first != null) {
        pageParam = new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT);
      } else {
        pageParam = new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV);
      }
      CursorPager<io.spring.application.data.ArticleData> result =
          articleQueryService.findRecentArticlesWithCursor(
              tag, author, favoritedBy, pageParam, user);
      PageInfo pageInfo =
          new PageInfo(
              result.hasNext(),
              result.hasPrevious(),
              result.getStartCursor() == null ? null : result.getStartCursor().toString(),
              result.getEndCursor() == null ? null : result.getEndCursor().toString());
      return ResponseEntity.ok(new ArticleCursorList(result.getData(), pageInfo));
    } else {
      int offsetVal = offset != null ? offset : 0;
      int limitVal = limit != null ? limit : 20;
      return ResponseEntity.ok(
          articleQueryService.findRecentArticles(
              tag, author, favoritedBy, new io.spring.application.Page(offsetVal, limitVal), user));
    }
  }
}
