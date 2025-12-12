package io.spring.api.exception;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorResourceSerializer extends JsonSerializer<ErrorResource> {
  @Override
  public void serialize(ErrorResource value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException, JsonProcessingException {
    Map<String, List<String>> errorMap = new HashMap<>();
    for (FieldErrorResource fieldErrorResource : value.getFieldErrors()) {
      if (!errorMap.containsKey(fieldErrorResource.getField())) {
        errorMap.put(fieldErrorResource.getField(), new ArrayList<String>());
      }
      errorMap.get(fieldErrorResource.getField()).add(fieldErrorResource.getMessage());
    }

    gen.writeStartObject();
    gen.writeStringField("message", value.getMessage());
    gen.writeArrayFieldStart("errors");
    for (Map.Entry<String, List<String>> pair : errorMap.entrySet()) {
      gen.writeStartObject();
      gen.writeStringField("key", pair.getKey());
      gen.writeArrayFieldStart("value");
      for (String errorMessage : pair.getValue()) {
        gen.writeString(errorMessage);
      }
      gen.writeEndArray();
      gen.writeEndObject();
    }
    gen.writeEndArray();
    gen.writeEndObject();
  }
}
