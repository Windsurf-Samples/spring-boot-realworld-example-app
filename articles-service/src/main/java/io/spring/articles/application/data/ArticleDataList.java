package io.spring.articles.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDataList {
  @JsonProperty("articles")
  private List<ArticleData> articleDatas;

  @JsonProperty("articlesCount")
  private int count;
}
