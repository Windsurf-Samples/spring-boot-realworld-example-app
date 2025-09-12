package io.spring.articles.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.relay.Connection;
import graphql.relay.SimpleListConnection;
import graphql.schema.DataFetchingEnvironment;
import io.spring.articles.application.ArticleQueryService;
import io.spring.articles.application.CursorPageParameter;
import io.spring.articles.application.CursorPager;
import io.spring.articles.application.DateTimeCursor;
import io.spring.articles.application.Page;
import io.spring.articles.application.data.ArticleData;
import io.spring.articles.application.data.ArticleDataList;
import io.spring.articles.core.article.Article;
import io.spring.articles.core.user.User;
import java.util.Optional;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class ArticleDatafetcher {
  private ArticleQueryService articleQueryService;

  @DgsData(parentType = "Query", field = "article")
  public ArticleData getArticle(@InputArgument("slug") String slug) {
    Optional<User> optionalUser = SecurityUtil.getCurrentUser();
    return articleQueryService
        .findBySlug(slug, optionalUser.orElse(null))
        .orElseThrow(() -> new RuntimeException("Article not found"));
  }

  @DgsData(parentType = "Query", field = "articles")
  public Connection<ArticleData> getArticles(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      @InputArgument("authoredBy") String authoredBy,
      @InputArgument("favoritedBy") String favoritedBy,
      @InputArgument("withTag") String withTag,
      DataFetchingEnvironment dfe) {
    Optional<User> optionalUser = SecurityUtil.getCurrentUser();

    if (first != null || after != null || last != null || before != null) {
      CursorPageParameter<DateTimeCursor> page =
          buildCursorPageParameter(first, after, last, before);
      CursorPager<ArticleData> result =
          articleQueryService.findRecentArticlesWithCursor(
              withTag, authoredBy, favoritedBy, page, optionalUser.orElse(null));
      return new SimpleListConnection<>(result.getData()).get(dfe);
    } else {
      ArticleDataList result =
          articleQueryService.findRecentArticles(
              withTag, authoredBy, favoritedBy, new Page(0, 20), optionalUser.orElse(null));
      return new SimpleListConnection<>(result.getArticleDatas()).get(dfe);
    }
  }

  @DgsData(parentType = "Query", field = "feed")
  public Connection<ArticleData> getFeed(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DataFetchingEnvironment dfe) {
    User user =
        SecurityUtil.getCurrentUser()
            .orElseThrow(() -> new RuntimeException("Authentication required"));

    if (first != null || after != null || last != null || before != null) {
      CursorPageParameter<DateTimeCursor> page =
          buildCursorPageParameter(first, after, last, before);
      CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);
      return new SimpleListConnection<>(result.getData()).get(dfe);
    } else {
      ArticleDataList result = articleQueryService.findUserFeed(user, new Page(0, 20));
      return new SimpleListConnection<>(result.getArticleDatas()).get(dfe);
    }
  }

  @DgsData(parentType = "ArticlePayload", field = "article")
  public ArticleData getArticlePayload(DataFetchingEnvironment dfe) {
    Article article = dfe.getLocalContext();
    Optional<User> optionalUser = SecurityUtil.getCurrentUser();
    return articleQueryService
        .findBySlug(article.getSlug(), optionalUser.orElse(null))
        .orElseThrow(() -> new RuntimeException("Article not found"));
  }

  private CursorPageParameter<DateTimeCursor> buildCursorPageParameter(
      Integer first, String after, Integer last, String before) {
    if (first != null) {
      return new CursorPageParameter<>(
          DateTimeCursor.parse(after), first, CursorPager.Direction.NEXT);
    } else {
      return new CursorPageParameter<>(
          DateTimeCursor.parse(before), last, CursorPager.Direction.PREV);
    }
  }
}
