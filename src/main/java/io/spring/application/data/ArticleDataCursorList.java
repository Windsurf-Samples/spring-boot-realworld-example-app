package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.CursorPager;
import java.util.List;
import lombok.Getter;

@Getter
public class ArticleDataCursorList {
  @JsonProperty("articles")
  private final List<ArticleData> articles;

  @JsonProperty("articlesCount")
  private final int count;

  @JsonProperty("hasNext")
  private final boolean hasNext;

  @JsonProperty("hasPrevious")
  private final boolean hasPrevious;

  @JsonProperty("startCursor")
  private final String startCursor;

  @JsonProperty("endCursor")
  private final String endCursor;

  public ArticleDataCursorList(CursorPager<ArticleData> pager) {
    this.articles = pager.getData();
    this.count = pager.getData().size();
    this.hasNext = pager.hasNext();
    this.hasPrevious = pager.hasPrevious();
    this.startCursor = pager.getStartCursor() != null ? pager.getStartCursor().toString() : null;
    this.endCursor = pager.getEndCursor() != null ? pager.getEndCursor().toString() : null;
  }
}
