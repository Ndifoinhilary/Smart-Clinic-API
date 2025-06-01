package org.bydefault.smartclinic.exception;

public class FileSizeExceptions extends RuntimeException{
    public FileSizeExceptions(String message) {
        super(message);
    }

    public FileSizeExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}
