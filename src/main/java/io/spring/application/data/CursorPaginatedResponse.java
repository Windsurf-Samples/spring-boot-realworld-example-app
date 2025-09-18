package io.spring.application.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CursorPaginatedResponse<T> {
  private List<T> data;
  private PageInfoData pageInfo;
}
