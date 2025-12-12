package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  public void should_set_next_true_when_direction_is_next_and_has_extra() {
    List<TestNode> data = createTestNodes(3);
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, true);

    assertThat(pager.hasNext(), is(true));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_set_next_false_when_direction_is_next_and_no_extra() {
    List<TestNode> data = createTestNodes(3);
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_set_previous_true_when_direction_is_prev_and_has_extra() {
    List<TestNode> data = createTestNodes(3);
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.PREV, true);

    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(true));
  }

  @Test
  public void should_set_previous_false_when_direction_is_prev_and_no_extra() {
    List<TestNode> data = createTestNodes(3);
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.PREV, false);

    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_return_null_start_cursor_for_empty_data() {
    List<TestNode> data = new ArrayList<>();
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getStartCursor(), nullValue());
  }

  @Test
  public void should_return_null_end_cursor_for_empty_data() {
    List<TestNode> data = new ArrayList<>();
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getEndCursor(), nullValue());
  }

  @Test
  public void should_return_first_element_cursor_as_start_cursor() {
    List<TestNode> data = createTestNodes(3);
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getStartCursor(), is(data.get(0).getCursor()));
  }

  @Test
  public void should_return_last_element_cursor_as_end_cursor() {
    List<TestNode> data = createTestNodes(3);
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getEndCursor(), is(data.get(2).getCursor()));
  }

  @Test
  public void should_return_data_list() {
    List<TestNode> data = createTestNodes(3);
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getData().size(), is(3));
    assertThat(pager.getData(), is(data));
  }

  @Test
  public void should_handle_single_element_list() {
    List<TestNode> data = createTestNodes(1);
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getStartCursor(), is(data.get(0).getCursor()));
    assertThat(pager.getEndCursor(), is(data.get(0).getCursor()));
  }

  private List<TestNode> createTestNodes(int count) {
    List<TestNode> nodes = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      nodes.add(new TestNode(new DateTime().plusHours(i)));
    }
    return nodes;
  }

  private static class TestNode implements Node {
    private final DateTimeCursor cursor;

    public TestNode(DateTime dateTime) {
      this.cursor = new DateTimeCursor(dateTime);
    }

    @Override
    public PageCursor getCursor() {
      return cursor;
    }
  }
}
