package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.CursorPager;
import java.util.List;
import lombok.Getter;

@Getter
public class ArticleDataListWithCursor {
  @JsonProperty("articles")
  private final List<ArticleData> articleDatas;

  @JsonProperty("pageInfo")
  private final PageInfo pageInfo;

  public ArticleDataListWithCursor(CursorPager<ArticleData> cursorPager) {
    this.articleDatas = cursorPager.getData();
    this.pageInfo =
        new PageInfo(
            cursorPager.getStartCursor() == null ? null : cursorPager.getStartCursor().toString(),
            cursorPager.getEndCursor() == null ? null : cursorPager.getEndCursor().toString(),
            cursorPager.hasNext(),
            cursorPager.hasPrevious());
  }
}
