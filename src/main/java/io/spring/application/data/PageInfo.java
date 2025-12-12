package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageInfo {
  private final String startCursor;
  private final String endCursor;
  private final boolean hasNextPage;
  private final boolean hasPreviousPage;
}
