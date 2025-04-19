package com.leave.management.system.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@RestController
public class ExceptionHandlerConfig {

    @ExceptionHandler
    public ResponseEntity<?> configureException(GlobalException ex, WebRequest web) {
        ExceptionObject obej = new ExceptionObject(ex.getMessage(),400);
        return new ResponseEntity<Object>(obej, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<String> handleResponseException(ResponseException responseException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseException.getMessage());
    }

    @ExceptionHandler(ResponseConflictException.class)
    public ResponseEntity<String> handleResponseConflictException(ResponseConflictException responseConflictException) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseConflictException.getMessage());
    }

    @ExceptionHandler({AuthException.class})
    public ResponseEntity<?> authExceptionHandler(AuthException authException) {
        ExceptionObject obej = new ExceptionObject(authException.getMessage(),401);
        return new ResponseEntity<Object>(obej, HttpStatus.UNAUTHORIZED);
    }

//    @org.springframework.web.bind.annotation.ExceptionHandler(BindException.class)
//    public ResponseEntity<Map<String, String>> handleBindException(BindException e) {
//        return getBindExceptionResponse(e.getBindingResult());
//    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String>> handleWebExchangeBindException(WebExchangeBindException e) {
        return getBindExceptionResponse(e.getBindingResult());
    }

    private ResponseEntity<Map<String, String>> getBindExceptionResponse(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors()
                .forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}