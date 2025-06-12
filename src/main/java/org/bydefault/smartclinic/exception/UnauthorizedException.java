package org.bydefault.smartclinic.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String s) {
        super(s);
    }
}
