package org.bydefault.smartclinic.exception;

public class BadCredentialsException  extends RuntimeException{
    public BadCredentialsException(String message) {
        super(message);
    }
}
