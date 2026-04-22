package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private ArticleData createArticleData(String id, DateTime updatedAt) {
    return new ArticleData(
        id,
        "slug-" + id,
        "title " + id,
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        updatedAt,
        new ArrayList<>(),
        new ProfileData("userId", "username", "bio", "image", false));
  }

  @Test
  public void should_set_next_when_direction_is_next_and_has_extra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1", new DateTime()));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_not_set_next_when_direction_is_next_and_no_extra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1", new DateTime()));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_set_previous_when_direction_is_prev_and_has_extra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1", new DateTime()));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, true);
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void should_not_set_previous_when_direction_is_prev_and_no_extra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1", new DateTime()));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  public void should_return_start_and_end_cursors() {
    DateTime time1 = new DateTime(2023, 1, 1, 0, 0);
    DateTime time2 = new DateTime(2023, 6, 1, 0, 0);
    List<ArticleData> data =
        Arrays.asList(createArticleData("1", time1), createArticleData("2", time2));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);
    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertEquals(String.valueOf(time1.getMillis()), pager.getStartCursor().toString());
    assertEquals(String.valueOf(time2.getMillis()), pager.getEndCursor().toString());
  }
}
