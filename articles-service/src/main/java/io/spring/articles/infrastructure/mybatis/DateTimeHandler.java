package io.spring.articles.infrastructure.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;

public class DateTimeHandler extends BaseTypeHandler<DateTime> {

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, DateTime parameter, JdbcType jdbcType) throws SQLException {
    ps.setTimestamp(i, new Timestamp(parameter.getMillis()));
  }

  @Override
  public DateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnName);
    if (timestamp != null) {
      return new DateTime(timestamp.getTime());
    }
    return null;
  }

  @Override
  public DateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnIndex);
    if (timestamp != null) {
      return new DateTime(timestamp.getTime());
    }
    return null;
  }

  @Override
  public DateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    Timestamp timestamp = cs.getTimestamp(columnIndex);
    if (timestamp != null) {
      return new DateTime(timestamp.getTime());
    }
    return null;
  }
}
