package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentEdge {
  private String cursor;
  private CommentData node;
}
