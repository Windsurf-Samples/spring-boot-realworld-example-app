package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.CommentData;
import io.spring.application.data.CommentEdge;
import io.spring.application.data.CommentsConnection;
import io.spring.application.data.PageInfo;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment = new Comment(newCommentParam.getBody(), user.getId(), article.getId());
    commentRepository.save(comment);
    return ResponseEntity.status(201)
        .body(commentResponse(commentQueryService.findById(comment.getId(), user).get()));
  }

  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug,
      @RequestParam(value = "first", required = false) Integer first,
      @RequestParam(value = "last", required = false) Integer last,
      @RequestParam(value = "after", required = false) String after,
      @RequestParam(value = "before", required = false) String before,
      @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    if (first != null || last != null) {
      return ResponseEntity.ok(
          getCommentsWithCursor(article.getId(), user, first, last, after, before));
    }

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  private CommentsConnection getCommentsWithCursor(
      String articleId, User user, Integer first, Integer last, String after, String before) {
    CursorPager<CommentData> comments;
    if (first != null) {
      comments =
          commentQueryService.findByArticleIdWithCursor(
              articleId,
              user,
              new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT));
    } else {
      comments =
          commentQueryService.findByArticleIdWithCursor(
              articleId,
              user,
              new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV));
    }
    return buildCommentsConnection(comments);
  }

  private CommentsConnection buildCommentsConnection(CursorPager<CommentData> comments) {
    PageInfo pageInfo =
        new PageInfo(
            comments.getStartCursor() != null ? comments.getStartCursor().toString() : null,
            comments.getEndCursor() != null ? comments.getEndCursor().toString() : null,
            comments.hasNext(),
            comments.hasPrevious());

    return new CommentsConnection(
        comments.getData().stream()
            .map(c -> new CommentEdge(c.getCursor().toString(), c))
            .collect(Collectors.toList()),
        pageInfo);
  }

  @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return commentRepository
        .findById(article.getId(), commentId)
        .map(
            comment -> {
              if (!AuthorizationService.canWriteComment(user, article, comment)) {
                throw new NoAuthorizationException();
              }
              commentRepository.remove(comment);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
