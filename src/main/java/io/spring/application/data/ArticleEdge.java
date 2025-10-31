package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticleEdge {
  private String cursor;
  private ArticleData node;
}
