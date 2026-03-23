package io.spring.auth.api.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({InvalidRequestException.class})
  public ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
    InvalidRequestException ire = (InvalidRequestException) e;

    List<Map<String, Object>> errorList = new ArrayList<>();
    ire.getErrors()
        .getFieldErrors()
        .forEach(
            fieldError -> {
              Map<String, Object> error = new HashMap<>();
              error.put("field", fieldError.getField());
              error.put("message", fieldError.getDefaultMessage());
              errorList.add(error);
            });

    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorList);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, body, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
  }

  @ExceptionHandler({InvalidAuthenticationException.class})
  public ResponseEntity<Object> handleInvalidAuthentication(
      RuntimeException e, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", e.getMessage());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, body, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
  }

  @ExceptionHandler({ResourceNotFoundException.class})
  public ResponseEntity<Object> handleNotFound(RuntimeException e, WebRequest request) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<Object> handleConstraintViolation(
      ConstraintViolationException e, WebRequest request) {
    List<Map<String, Object>> errorList = new ArrayList<>();
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      Map<String, Object> error = new HashMap<>();
      error.put("field", violation.getPropertyPath().toString());
      error.put("message", violation.getMessage());
      errorList.add(error);
    }

    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorList);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, body, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
  }
}
