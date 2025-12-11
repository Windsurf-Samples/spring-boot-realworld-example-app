package io.spring.api.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import lombok.Getter;

@JsonSerialize(using = ErrorResourceSerializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class ErrorResource {
  private String message;
  private List<FieldErrorResource> fieldErrors;

  public ErrorResource(List<FieldErrorResource> fieldErrorResources) {
    this.message = "VALIDATION_ERROR";
    this.fieldErrors = fieldErrorResources;
  }

  public ErrorResource(String message, List<FieldErrorResource> fieldErrorResources) {
    this.message = message;
    this.fieldErrors = fieldErrorResources;
  }
}
