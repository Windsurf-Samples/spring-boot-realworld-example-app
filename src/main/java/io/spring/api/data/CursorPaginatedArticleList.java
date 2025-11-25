package io.spring.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import java.util.List;
import lombok.Getter;

@Getter
public class CursorPaginatedArticleList {
  @JsonProperty("articles")
  private final List<ArticleData> articleDatas;

  @JsonProperty("pageInfo")
  private final CursorPageInfo pageInfo;

  public CursorPaginatedArticleList(CursorPager<ArticleData> cursorPager) {
    this.articleDatas = cursorPager.getData();
    this.pageInfo =
        new CursorPageInfo(
            cursorPager.getStartCursor() == null ? null : cursorPager.getStartCursor().toString(),
            cursorPager.getEndCursor() == null ? null : cursorPager.getEndCursor().toString(),
            cursorPager.hasNext(),
            cursorPager.hasPrevious());
  }
}
