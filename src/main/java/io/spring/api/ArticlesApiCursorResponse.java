package io.spring.api;

import io.spring.application.data.ArticleData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesApiCursorResponse {
  private List<ArticleData> articles;
  private PageInfo pageInfo;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PageInfo {
    private String startCursor;
    private String endCursor;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
  }
}
