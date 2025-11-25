package io.spring.api.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageInfo {
  private String startCursor;
  private String endCursor;
  private boolean hasNextPage;
  private boolean hasPreviousPage;
}
