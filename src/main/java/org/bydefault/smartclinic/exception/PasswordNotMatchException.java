package org.bydefault.smartclinic.exception;

public class PasswordNotMatchException extends RuntimeException {
    private String message;
    public PasswordNotMatchException(String message) {
        super(message);
    }
}
