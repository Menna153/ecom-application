package com.app.ecom_application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        String validationCode = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        int code = Integer.parseInt(validationCode);

        ErrorCode error = ErrorCode.fromCode(code);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(error.getMessage(), error.getCode()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {

        ErrorCode error = ErrorCode.UNAUTHORIZED_ACTION_BY_USER;

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        error.getMessage(),
                        error.getCode()
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ErrorCode.INVALID_USERNAME_OR_PASSWORD.getMessage(), ErrorCode.INVALID_USERNAME_OR_PASSWORD.getCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ex.printStackTrace();

        ErrorCode error = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(error.getMessage(), error.getCode()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex) {

        switch (ex.getMessage()) {

            case "PRODUCT_NAME_EXISTS":
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(
                                ErrorCode.PRODUCT_NAME_EXISTS.getMessage(),
                                ErrorCode.PRODUCT_NAME_EXISTS.getCode()));

            case "INVALID_PRODUCT_ID":
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                ErrorCode.INVALID_PRODUCT_ID.getMessage(),
                                ErrorCode.INVALID_PRODUCT_ID.getCode()));

            case "INVALID_ORDER_ID":
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                ErrorCode.INVALID_ORDER_ID.getMessage(),
                                ErrorCode.INVALID_ORDER_ID.getCode()));

            case "INVALID_TOKEN":
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(
                                ErrorCode.INVALID_TOKEN.getMessage(),
                                ErrorCode.INVALID_TOKEN.getCode()));

            default:
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Bad request", 4000));
        }
    }
}
