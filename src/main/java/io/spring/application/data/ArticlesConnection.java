package io.spring.application.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticlesConnection {
  private List<ArticleEdge> edges;
  private PageInfo pageInfo;
}
