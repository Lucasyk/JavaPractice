package com.example.demo.exception;

import com.example.demo.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
    MethodArgumentNotValidException exception,
        HttpServletRequest request
  ) {
    Map<String, String> fieldErrors = new HashMap<>();

    exception.getBindingResult()
    .getFieldErrors()
    .forEach(error ->
      fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
    
    ApiErrorResponse response = new ApiErrorResponse(
      Instant.now(),
          400,
        "Bad Request",
                  "Validation failed",
                  request.getRequestURI(),
                      fieldErrors
    );

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(PlayerNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handlePlayerNotFound(PlayerNotFoundException exception,
      HttpServletRequest request) {

    ApiErrorResponse response = new ApiErrorResponse(
        Instant.now(),
        404,
        "Not Found",
        exception.getMessage(),
        request.getRequestURI(),
        Map.of());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }
  
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleMalformedJson(
    HttpMessageNotReadableException exception,
        HttpServletRequest request
  ) {
    ApiErrorResponse response = new ApiErrorResponse(
      Instant.now(),
          400,
              "Bad Request",
                  "Malformed JSON request",
                      request.getRequestURI(),
                          Map.of()
    );

    return ResponseEntity.badRequest().body(response);
  }
}
