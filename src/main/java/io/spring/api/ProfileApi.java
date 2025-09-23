package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;

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

  private ResponseEntity profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", profile);
          }
        });
  }

  @GetMapping(path = "articles")
  public ResponseEntity getProfileArticles(
      @PathVariable("username") String username,
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User user) {

    if (first != null || last != null || after != null || before != null) {
      if (first == null && last == null) {
        throw new IllegalArgumentException(
            "Either 'first' or 'last' must be provided for cursor pagination");
      }

      CursorPager<ArticleData> articles;
      if (first != null) {
        articles =
            articleQueryService.findRecentArticlesWithCursor(
                null,
                username,
                null,
                new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT),
                user);
      } else {
        articles =
            articleQueryService.findRecentArticlesWithCursor(
                null,
                username,
                null,
                new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV),
                user);
      }

      return ResponseEntity.ok(buildCursorResponse(articles));
    }

    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(
            null, username, null, new io.spring.application.Page(offset, limit), user));
  }

  @GetMapping(path = "favorites")
  public ResponseEntity getProfileFavorites(
      @PathVariable("username") String username,
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User user) {

    if (first != null || last != null || after != null || before != null) {
      if (first == null && last == null) {
        throw new IllegalArgumentException(
            "Either 'first' or 'last' must be provided for cursor pagination");
      }

      CursorPager<ArticleData> articles;
      if (first != null) {
        articles =
            articleQueryService.findRecentArticlesWithCursor(
                null,
                null,
                username,
                new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT),
                user);
      } else {
        articles =
            articleQueryService.findRecentArticlesWithCursor(
                null,
                null,
                username,
                new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV),
                user);
      }

      return ResponseEntity.ok(buildCursorResponse(articles));
    }

    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(
            null, null, username, new io.spring.application.Page(offset, limit), user));
  }

  @GetMapping(path = "feed")
  public ResponseEntity getProfileFeed(
      @PathVariable("username") String username,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User user) {

    if (first == null && last == null) {
      throw new IllegalArgumentException(
          "Either 'first' or 'last' must be provided for cursor pagination");
    }

    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

    CursorPager<ArticleData> articles;
    if (first != null) {
      articles =
          articleQueryService.findUserFeedWithCursor(
              target,
              new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT));
    } else {
      articles =
          articleQueryService.findUserFeedWithCursor(
              target,
              new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV));
    }

    return ResponseEntity.ok(buildCursorResponse(articles));
  }

  private Map<String, Object> buildCursorResponse(CursorPager<ArticleData> articles) {
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
      }
    };
  }
}
