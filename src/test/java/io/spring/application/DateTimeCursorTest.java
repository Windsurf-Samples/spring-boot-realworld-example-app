package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_convert_to_string_as_millis() {
    DateTime dateTime = new DateTime(2023, 6, 1, 12, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);
    assertEquals(String.valueOf(dateTime.getMillis()), cursor.toString());
  }

  @Test
  public void should_parse_millis_string_to_datetime() {
    DateTime original = new DateTime(2023, 6, 1, 12, 0, 0, DateTimeZone.UTC);
    String millis = String.valueOf(original.getMillis());
    DateTime parsed = DateTimeCursor.parse(millis);
    assertNotNull(parsed);
    assertEquals(original.getMillis(), parsed.getMillis());
    assertEquals(DateTimeZone.UTC, parsed.getZone());
  }

  @Test
  public void should_return_null_for_null_cursor() {
    assertNull(DateTimeCursor.parse(null));
  }

  @Test
  public void should_store_data() {
    DateTime dateTime = new DateTime(2023, 1, 15, 0, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);
    assertEquals(dateTime, cursor.getData());
  }
}
