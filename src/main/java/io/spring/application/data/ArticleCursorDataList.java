package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.CursorPager;
import java.util.List;
import lombok.Getter;

@Getter
public class ArticleCursorDataList {
  @JsonProperty("articles")
  private final List<ArticleData> articleDatas;

  @JsonProperty("pageInfo")
  private final PageInfo pageInfo;

  public ArticleCursorDataList(CursorPager<ArticleData> cursorPager) {
    this.articleDatas = cursorPager.getData();
    this.pageInfo =
        new PageInfo(
            cursorPager.getStartCursor() != null ? cursorPager.getStartCursor().toString() : null,
            cursorPager.getEndCursor() != null ? cursorPager.getEndCursor().toString() : null,
            cursorPager.hasNext(),
            cursorPager.hasPrevious());
  }
}
