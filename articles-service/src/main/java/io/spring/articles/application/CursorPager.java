package io.spring.articles.application;

import java.util.List;
import lombok.Getter;

@Getter
public class CursorPager<T extends Node> {
  public enum Direction {
    NEXT,
    PREV
  }

  private List<T> data;
  private boolean hasNext;
  private boolean hasPrevious;
  private PageCursor startCursor;
  private PageCursor endCursor;

  public CursorPager(List<T> data, boolean hasNext, boolean hasPrevious) {
    this.data = data;
    this.hasNext = hasNext;
    this.hasPrevious = hasPrevious;
    if (!data.isEmpty()) {
      this.startCursor = data.get(0).getCursor();
      this.endCursor = data.get(data.size() - 1).getCursor();
    }
  }
}
