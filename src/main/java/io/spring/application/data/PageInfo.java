package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageInfo {
  @JsonProperty("hasNextPage")
  private final boolean hasNext;

  @JsonProperty("hasPreviousPage")
  private final boolean hasPrevious;

  @JsonProperty("startCursor")
  private final String startCursor;

  @JsonProperty("endCursor")
  private final String endCursor;
}
