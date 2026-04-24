package com.banking.banking_app_apis.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setType(URI.create("https://example.com/not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create("/resource/not-found"));
        return problemDetail;
    }


    @ExceptionHandler(value = DuplicateAccountException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicateAccountException(DuplicateAccountException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setType(URI.create("https://example.com/not-found"));
        problemDetail.setTitle("Duplicate Account Found");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create("/resource/not-found"));
        return problemDetail;
    }


    @ExceptionHandler(value = InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.INSUFFICIENT_STORAGE)
    public ProblemDetail handleInsufficiantBalanceException(InsufficientBalanceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INSUFFICIENT_STORAGE);
        problemDetail.setType(URI.create("https://example.com/not-found"));
        problemDetail.setTitle("Insufficiant Balance");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create("/resource/not-found"));
        return problemDetail;
    }


    @ExceptionHandler(value = ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidationException(ValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create("https://example.com/not-found"));
        problemDetail.setTitle("Validation Exception");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create("/resource/not-found"));
        return problemDetail;
    }

}
