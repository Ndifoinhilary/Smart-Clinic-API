package org.bydefault.smartclinic.exception;

public class ExpiredVerificationCodeException extends RuntimeException{
    public ExpiredVerificationCodeException(String message) {
        super(message);
    }
}
