package io.spring.application.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentsConnection {
  private List<CommentEdge> edges;
  private PageInfo pageInfo;
}
