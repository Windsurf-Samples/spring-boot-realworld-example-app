package io.spring.articles.api.exception;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class CustomizeExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResource> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception, WebRequest request) {
    ErrorResource errorResource = new ErrorResource();

    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      errorResource.addFieldError(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return new ResponseEntity<>(errorResource, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<ErrorResource> handleConstraintViolationException(
      ConstraintViolationException exception, WebRequest request) {
    ErrorResource errorResource = new ErrorResource();
    for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
      String fieldName = violation.getPropertyPath().toString();
      String errorMessage = violation.getMessage();
      errorResource.addFieldError(fieldName, errorMessage);
    }
    return new ResponseEntity<>(errorResource, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity handleResourceNotFoundException() {
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
