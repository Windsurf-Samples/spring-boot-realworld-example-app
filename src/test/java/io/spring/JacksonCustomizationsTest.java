package io.spring;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.StringWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class JacksonCustomizationsTest {

  @Test
  public void should_serialize_datetime_to_iso_format() throws Exception {
    JacksonCustomizations.DateTimeSerializer serializer =
        new JacksonCustomizations.DateTimeSerializer();
    DateTime dateTime = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);

    StringWriter writer = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = new ObjectMapper().getSerializerProvider();

    serializer.serialize(dateTime, gen, provider);
    gen.flush();

    String result = writer.toString();
    assertTrue(result.contains("2023"));
    assertTrue(result.contains("06"));
    assertTrue(result.contains("15"));
  }

  @Test
  public void should_serialize_null_datetime_as_null() throws Exception {
    JacksonCustomizations.DateTimeSerializer serializer =
        new JacksonCustomizations.DateTimeSerializer();

    StringWriter writer = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = new ObjectMapper().getSerializerProvider();

    serializer.serialize(null, gen, provider);
    gen.flush();

    assertEquals("null", writer.toString());
  }

  @Test
  public void should_create_real_world_module() {
    JacksonCustomizations customizations = new JacksonCustomizations();
    assertNotNull(customizations.realWorldModules());
  }
}
