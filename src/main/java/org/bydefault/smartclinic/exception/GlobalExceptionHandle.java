package org.bydefault.smartclinic.exception;

import org.bydefault.smartclinic.dtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        var errorResponse = new ErrorResponse();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setErrorCode(HttpStatus.NOT_FOUND.value());
        errorResponse.setDetails(request.getDescription(false));
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({UserAlreadyExistException.class})
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(UserAlreadyExistException ex, WebRequest request) {
        var errorResponse = new ErrorResponse();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setDetails(request.getDescription(false));
        errorResponse.setErrorCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var error = new HashMap<String, String>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            error.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VerificationException.class)
    public ResponseEntity<ErrorResponse> handleVerificationException(VerificationException e, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(e.getMessage());
        error.setDetails(request.getDescription(false));
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ExpiredVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCodeException(ExpiredVerificationCodeException e, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(e.getMessage());
        error.setDetails(request.getDescription(false));
        error.setErrorCode(HttpStatus.GONE.value());
        return ResponseEntity.status(HttpStatus.GONE).body(error);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ErrorResponse> handleEmailException(EmailException e, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(e.getMessage());
        error.setDetails(request.getDescription(false));
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordNotMatchException(PasswordNotMatchException e, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(e.getMessage());
        error.setDetails(request.getDescription(false));
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(e.getMessage());
        error.setDetails(request.getDescription(false));
        error.setErrorCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotVerifiedException(UserNotVerifiedException e, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(e.getMessage());
        error.setDetails(request.getDescription(false));
        error.setErrorCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UserRoleException.class)
    public ResponseEntity<ErrorResponse> handleUserRoleException(UserRoleException e, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(e.getMessage());
        error.setDetails(request.getDescription(false));
        error.setErrorCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

}
