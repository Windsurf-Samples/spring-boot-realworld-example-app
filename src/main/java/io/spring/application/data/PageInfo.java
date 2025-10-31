package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageInfo {
  private String startCursor;
  private String endCursor;
  private boolean hasPreviousPage;
  private boolean hasNextPage;
}
