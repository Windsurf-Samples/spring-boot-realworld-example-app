package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_create_cursor_with_datetime() {
    DateTime dateTime = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertThat(cursor.getData(), is(dateTime));
  }

  @Test
  public void should_return_millis_as_string() {
    DateTime dateTime = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertThat(cursor.toString(), is(String.valueOf(dateTime.getMillis())));
  }

  @Test
  public void should_parse_cursor_string_to_datetime() {
    DateTime originalDateTime = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    String cursorString = String.valueOf(originalDateTime.getMillis());

    DateTime parsed = DateTimeCursor.parse(cursorString);

    assertThat(parsed, notNullValue());
    assertThat(parsed.getMillis(), is(originalDateTime.getMillis()));
  }

  @Test
  public void should_return_null_when_parsing_null_cursor() {
    DateTime parsed = DateTimeCursor.parse(null);

    assertThat(parsed, nullValue());
  }

  @Test
  public void should_parse_cursor_with_utc_timezone() {
    DateTime originalDateTime = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    String cursorString = String.valueOf(originalDateTime.getMillis());

    DateTime parsed = DateTimeCursor.parse(cursorString);

    assertThat(parsed.getZone(), is(DateTimeZone.UTC));
  }

  @Test
  public void should_roundtrip_cursor_through_string() {
    DateTime originalDateTime = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(originalDateTime);

    String cursorString = cursor.toString();
    DateTime parsed = DateTimeCursor.parse(cursorString);

    assertThat(parsed.getMillis(), is(originalDateTime.getMillis()));
  }

  @Test
  public void should_handle_current_datetime() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);

    assertThat(cursor.getData().getMillis(), is(now.getMillis()));
  }

  @Test
  public void should_handle_epoch_datetime() {
    DateTime epoch = new DateTime(0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(epoch);

    assertThat(cursor.toString(), is("0"));
    assertThat(DateTimeCursor.parse("0").getMillis(), is(0L));
  }
}
