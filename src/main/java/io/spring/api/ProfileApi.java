package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.DateTimeCursor;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleEdge;
import io.spring.application.data.ArticlesConnection;
import io.spring.application.data.PageInfo;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "profiles/{username}")
@AllArgsConstructor
public class ProfileApi {
  private ProfileQueryService profileQueryService;
  private UserRepository userRepository;
  private ArticleQueryService articleQueryService;

  @GetMapping
  public ResponseEntity getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return profileQueryService
        .findByUsername(username, user)
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PostMapping(path = "follow")
  public ResponseEntity follow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return userRepository
        .findByUsername(username)
        .map(
            target -> {
              FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
              userRepository.saveRelation(followRelation);
              return profileResponse(profileQueryService.findByUsername(username, user).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    Optional<User> userOptional = userRepository.findByUsername(username);
    if (userOptional.isPresent()) {
      User target = userOptional.get();
      return userRepository
          .findRelation(user.getId(), target.getId())
          .map(
              relation -> {
                userRepository.removeRelation(relation);
                return profileResponse(profileQueryService.findByUsername(username, user).get());
              })
          .orElseThrow(ResourceNotFoundException::new);
    } else {
      throw new ResourceNotFoundException();
    }
  }

  @GetMapping(path = "feed")
  public ResponseEntity getUserFeed(
      @PathVariable("username") String username,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User currentUser) {

    User targetUser =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

    if (first == null && last == null) {
      throw new IllegalArgumentException("Either 'first' or 'last' parameter must be provided");
    }
    if (first != null && last != null) {
      throw new IllegalArgumentException("Cannot provide both 'first' and 'last' parameters");
    }

    CursorPageParameter<org.joda.time.DateTime> pageParam;
    if (first != null) {
      pageParam =
          new CursorPageParameter<>(
              DateTimeCursor.parse(after), first, io.spring.application.CursorPager.Direction.NEXT);
    } else {
      pageParam =
          new CursorPageParameter<>(
              DateTimeCursor.parse(before), last, io.spring.application.CursorPager.Direction.PREV);
    }

    CursorPager<ArticleData> articles =
        articleQueryService.findUserFeedWithCursor(targetUser, pageParam);

    return ResponseEntity.ok(buildArticlesConnection(articles));
  }

  @GetMapping(path = "favorites")
  public ResponseEntity getUserFavorites(
      @PathVariable("username") String username,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User currentUser) {

    if (first == null && last == null) {
      throw new IllegalArgumentException("Either 'first' or 'last' parameter must be provided");
    }
    if (first != null && last != null) {
      throw new IllegalArgumentException("Cannot provide both 'first' and 'last' parameters");
    }

    CursorPageParameter<org.joda.time.DateTime> pageParam;
    if (first != null) {
      pageParam =
          new CursorPageParameter<>(
              DateTimeCursor.parse(after), first, io.spring.application.CursorPager.Direction.NEXT);
    } else {
      pageParam =
          new CursorPageParameter<>(
              DateTimeCursor.parse(before), last, io.spring.application.CursorPager.Direction.PREV);
    }

    CursorPager<ArticleData> articles =
        articleQueryService.findRecentArticlesWithCursor(
            null, null, username, pageParam, currentUser);

    return ResponseEntity.ok(buildArticlesConnection(articles));
  }

  @GetMapping(path = "articles")
  public ResponseEntity getUserArticles(
      @PathVariable("username") String username,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User currentUser) {

    if (first == null && last == null) {
      throw new IllegalArgumentException("Either 'first' or 'last' parameter must be provided");
    }
    if (first != null && last != null) {
      throw new IllegalArgumentException("Cannot provide both 'first' and 'last' parameters");
    }

    CursorPageParameter<org.joda.time.DateTime> pageParam;
    if (first != null) {
      pageParam =
          new CursorPageParameter<>(
              DateTimeCursor.parse(after), first, io.spring.application.CursorPager.Direction.NEXT);
    } else {
      pageParam =
          new CursorPageParameter<>(
              DateTimeCursor.parse(before), last, io.spring.application.CursorPager.Direction.PREV);
    }

    CursorPager<ArticleData> articles =
        articleQueryService.findRecentArticlesWithCursor(
            null, username, null, pageParam, currentUser);

    return ResponseEntity.ok(buildArticlesConnection(articles));
  }

  private HashMap<String, Object> buildArticlesConnection(CursorPager<ArticleData> articles) {
    PageInfo pageInfo =
        new PageInfo(
            articles.getStartCursor() == null ? null : articles.getStartCursor().toString(),
            articles.getEndCursor() == null ? null : articles.getEndCursor().toString(),
            articles.hasPrevious(),
            articles.hasNext());

    ArticlesConnection connection =
        new ArticlesConnection(
            articles.getData().stream()
                .map(a -> new ArticleEdge(a.getCursor().toString(), a))
                .collect(Collectors.toList()),
            pageInfo);

    return new HashMap<String, Object>() {
      {
        put("articlesConnection", connection);
      }
    };
  }

  private ResponseEntity profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", profile);
          }
        });
  }
}
