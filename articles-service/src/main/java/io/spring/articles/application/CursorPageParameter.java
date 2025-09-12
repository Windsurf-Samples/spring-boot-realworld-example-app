package io.spring.articles.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageParameter<T extends PageCursor> {
  private T cursor;
  private int limit;
  private CursorPager.Direction direction;
}
