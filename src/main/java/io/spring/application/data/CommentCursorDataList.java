package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.CursorPager;
import java.util.List;
import lombok.Getter;

@Getter
public class CommentCursorDataList {
  @JsonProperty("comments")
  private final List<CommentData> commentDatas;

  @JsonProperty("pageInfo")
  private final PageInfo pageInfo;

  public CommentCursorDataList(CursorPager<CommentData> cursorPager) {
    this.commentDatas = cursorPager.getData();
    this.pageInfo =
        new PageInfo(
            cursorPager.getStartCursor() != null ? cursorPager.getStartCursor().toString() : null,
            cursorPager.getEndCursor() != null ? cursorPager.getEndCursor().toString() : null,
            cursorPager.hasNext(),
            cursorPager.hasPrevious());
  }
}
