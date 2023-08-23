package com.dfc.exchange_api.backend.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

/**
 * Exception Handler for the Custom Exceptions Created in this REST API.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private ResponseEntity<Object> buildResponseEntity(ErrorDetails apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(ExternalApiConnectionError.class)
    protected ResponseEntity<Object> handleIncorrectParameter(ExternalApiConnectionError ex){
        ErrorDetails apiError = new ErrorDetails(HttpStatus.BAD_GATEWAY);
        apiError.setMessage(ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(InvalidCurrencyException.class)
    protected ResponseEntity<Object> handleIncorrectParameter(InvalidCurrencyException ex){
        ErrorDetails apiError = new ErrorDetails(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        ErrorDetails apiError = new ErrorDetails(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(CacheNotFoundException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(CacheNotFoundException ex) {
        ErrorDetails apiError = new ErrorDetails(HttpStatus.NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return buildResponseEntity(apiError);
    }

}
