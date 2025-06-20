package org.bydefault.smartclinic.exception;

public class UnAuthorizedException extends RuntimeException {
    public UnAuthorizedException(String s) {
        super(s);
    }
}
