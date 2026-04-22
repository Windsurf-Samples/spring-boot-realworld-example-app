package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void should_create_with_defaults() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
    assertEquals(Direction.NEXT, param.getDirection());
  }

  @Test
  public void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  public void should_keep_default_limit_for_zero_or_negative() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.NEXT);
    assertEquals(20, param.getLimit());

    CursorPageParameter<DateTime> paramNeg = new CursorPageParameter<>(null, -5, Direction.NEXT);
    assertEquals(20, paramNeg.getLimit());
  }

  @Test
  public void should_return_query_limit_as_limit_plus_one() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);
    assertEquals(11, param.getQueryLimit());
  }

  @Test
  public void should_return_is_next_true_for_next_direction() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);
    assertTrue(param.isNext());
  }

  @Test
  public void should_return_is_next_false_for_prev_direction() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.PREV);
    assertFalse(param.isNext());
  }

  @Test
  public void should_store_cursor_value() {
    DateTime cursor = new DateTime(2023, 6, 1, 0, 0);
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);
    assertEquals(cursor, param.getCursor());
  }
}
