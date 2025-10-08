package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.CursorPager;
import java.util.List;
import lombok.Getter;

@Getter
public class CommentDataCursorList {
  @JsonProperty("comments")
  private final List<CommentData> comments;

  @JsonProperty("commentsCount")
  private final int count;

  @JsonProperty("hasNext")
  private final boolean hasNext;

  @JsonProperty("hasPrevious")
  private final boolean hasPrevious;

  @JsonProperty("startCursor")
  private final String startCursor;

  @JsonProperty("endCursor")
  private final String endCursor;

  public CommentDataCursorList(CursorPager<CommentData> pager) {
    this.comments = pager.getData();
    this.count = pager.getData().size();
    this.hasNext = pager.hasNext();
    this.hasPrevious = pager.hasPrevious();
    this.startCursor = pager.getStartCursor() != null ? pager.getStartCursor().toString() : null;
    this.endCursor = pager.getEndCursor() != null ? pager.getEndCursor().toString() : null;
  }
}
