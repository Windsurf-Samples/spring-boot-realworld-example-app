package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentCursorList {
  @JsonProperty("comments")
  private final List<CommentData> comments;

  @JsonProperty("pageInfo")
  private final PageInfo pageInfo;
}
