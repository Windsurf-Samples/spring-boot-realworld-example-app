package io.spring.api.dto;

import io.spring.application.data.CommentData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEdge {
  private String cursor;
  private CommentData node;
}
