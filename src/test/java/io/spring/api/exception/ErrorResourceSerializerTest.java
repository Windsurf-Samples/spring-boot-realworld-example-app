package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  private final ErrorResourceSerializer serializer = new ErrorResourceSerializer();
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void should_serialize_single_field_error() throws Exception {
    FieldErrorResource fieldError =
        new FieldErrorResource("article", "title", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError));

    StringWriter writer = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(errorResource, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("title"));
    assertTrue(json.contains("can't be empty"));
  }

  @Test
  public void should_serialize_multiple_errors_for_same_field() throws Exception {
    FieldErrorResource error1 =
        new FieldErrorResource("user", "email", "NotBlank", "can't be empty");
    FieldErrorResource error2 =
        new FieldErrorResource("user", "email", "Email", "should be an email");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(error1, error2));

    StringWriter writer = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(errorResource, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("email"));
    assertTrue(json.contains("can't be empty"));
    assertTrue(json.contains("should be an email"));
  }

  @Test
  public void should_serialize_empty_errors() throws Exception {
    ErrorResource errorResource = new ErrorResource(Collections.emptyList());

    StringWriter writer = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(errorResource, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("errors"));
  }

  @Test
  public void should_serialize_errors_for_different_fields() throws Exception {
    FieldErrorResource error1 =
        new FieldErrorResource("user", "email", "NotBlank", "can't be empty");
    FieldErrorResource error2 =
        new FieldErrorResource("user", "username", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(error1, error2));

    StringWriter writer = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(errorResource, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("email"));
    assertTrue(json.contains("username"));
  }
}
