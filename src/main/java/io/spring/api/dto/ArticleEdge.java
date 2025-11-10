package io.spring.api.dto;

import io.spring.application.data.ArticleData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEdge {
  private String cursor;
  private ArticleData node;
}
