package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageInfoData {
  private String startCursor;
  private String endCursor;
  private boolean hasNextPage;
  private boolean hasPreviousPage;
}
