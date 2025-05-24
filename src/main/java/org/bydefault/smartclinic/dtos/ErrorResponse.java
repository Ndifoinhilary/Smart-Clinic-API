package org.bydefault.smartclinic.dtos;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private String details;
    private long errorCode;
}
