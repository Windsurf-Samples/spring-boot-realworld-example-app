package io.spring.articles.application;

import io.spring.articles.application.data.ArticleData;
import io.spring.articles.application.data.ArticleDataList;
import io.spring.articles.application.data.ArticleFavoriteCount;
import io.spring.articles.core.user.User;
import io.spring.articles.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.articles.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.articles.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ArticleFavoritesReadService articleFavoritesReadService;

  public Optional<ArticleData> findById(String id, User user) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (user != null) {
        fillExtraInfo(id, user, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public Optional<ArticleData> findBySlug(String slug, User user) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (user != null) {
        fillExtraInfo(articleData.getId(), user, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, User currentUser) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUser);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findUserFeed(User user, Page page) {
    List<String> followdUsers = userRelationshipQueryService.followedUsers(user.getId());
    if (followdUsers.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followdUsers, page);
      fillExtraInfo(articles, user);
      int count = articleReadService.countFeedSize(followdUsers);
      return new ArticleDataList(articles, count);
    }
  }

  public CursorPager<ArticleData> findRecentArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      CursorPageParameter<DateTimeCursor> page,
      User currentUser) {
    List<String> articleIds =
        articleReadService.findArticlesWithCursor(tag, author, favoritedBy, page);
    if (articleIds.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), false, false);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUser);

      boolean hasNext = articles.size() == page.getLimit();
      boolean hasPrevious = page.getCursor() != null;

      return new CursorPager<>(articles, hasNext, hasPrevious);
    }
  }

  public CursorPager<ArticleData> findUserFeedWithCursor(
      User user, CursorPageParameter<DateTimeCursor> page) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(user.getId());
    if (followedUsers.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), false, false);
    } else {
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthorsWithCursor(followedUsers, page);
      fillExtraInfo(articles, user);

      boolean hasNext = articles.size() == page.getLimit();
      boolean hasPrevious = page.getCursor() != null;

      return new CursorPager<>(articles, hasNext, hasPrevious);
    }
  }

  private void fillExtraInfo(String id, User currentUser, ArticleData articleData) {
    articleData.setFavorited(articleFavoritesReadService.isUserFavorite(currentUser.getId(), id));
  }

  private void fillExtraInfo(List<ArticleData> articles, User currentUser) {
    setFavoriteCount(articles);
    if (currentUser != null) {
      setIsFavorite(articles, currentUser);
      setIsFollowingAuthor(articles, currentUser);
    }
  }

  private void setIsFollowingAuthor(List<ArticleData> articles, User currentUser) {
    Set<String> followingAuthors =
        userRelationshipQueryService.followingAuthors(
            currentUser.getId(),
            articles.stream()
                .map(articleData1 -> articleData1.getProfileData().getId())
                .collect(Collectors.toList()));
    articles.forEach(
        articleData -> {
          if (followingAuthors.contains(articleData.getProfileData().getId())) {
            articleData.getProfileData().setFollowing(true);
          }
        });
  }

  private void setIsFavorite(List<ArticleData> articles, User currentUser) {
    Set<String> favoritedArticles =
        articleFavoritesReadService.userFavorites(
            articles.stream().map(ArticleData::getId).collect(Collectors.toList()), currentUser);

    articles.forEach(
        articleData -> {
          if (favoritedArticles.contains(articleData.getId())) {
            articleData.setFavorited(true);
          }
        });
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    List<ArticleFavoriteCount> favoritesCounts =
        articleFavoritesReadService.articlesFavoriteCount(
            articles.stream().map(ArticleData::getId).collect(Collectors.toList()));
    Map<String, Integer> countMap = new HashMap<>();
    favoritesCounts.forEach(
        item -> {
          countMap.put(item.getId(), item.getCount());
        });
    articles.forEach(
        articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
  }
}
