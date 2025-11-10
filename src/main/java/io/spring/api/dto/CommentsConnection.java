package io.spring.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentsConnection {
  private List<CommentEdge> edges;
  private PageInfo pageInfo;
}
