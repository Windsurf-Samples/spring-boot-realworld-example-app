package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesConnection {
  @JsonProperty("edges")
  private List<ArticleEdge> edges;

  @JsonProperty("pageInfo")
  private PageInfo pageInfo;
}
