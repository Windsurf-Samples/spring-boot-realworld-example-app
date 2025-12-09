package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {
  private String startCursor;
  private String endCursor;
  private boolean hasNextPage;
  private boolean hasPreviousPage;
}
