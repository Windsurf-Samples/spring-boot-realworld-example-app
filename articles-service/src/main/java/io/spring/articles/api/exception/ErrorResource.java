package io.spring.articles.api.exception;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonRootName("errors")
public class ErrorResource {
  private Map<String, List<String>> errors = new HashMap<>();

  public Map<String, List<String>> getErrors() {
    return errors;
  }

  public void addFieldError(String path, String message) {
    List<String> fieldErrors = errors.get(path);
    if (fieldErrors == null) {
      fieldErrors = new ArrayList<>();
      errors.put(path, fieldErrors);
    }
    fieldErrors.add(message);
  }
}
