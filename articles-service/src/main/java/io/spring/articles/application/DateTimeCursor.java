package io.spring.articles.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateTimeCursor implements PageCursor {
  private DateTime dateTime;

  public static DateTimeCursor parse(String cursor) {
    if (cursor == null) {
      return null;
    }
    return new DateTimeCursor(ISODateTimeFormat.dateTime().parseDateTime(cursor));
  }

  @Override
  public String toString() {
    return ISODateTimeFormat.dateTime().withZoneUTC().print(dateTime);
  }
}
