package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleCursorList {
  @JsonProperty("articles")
  private final List<ArticleData> articles;

  @JsonProperty("pageInfo")
  private final PageInfo pageInfo;
}
