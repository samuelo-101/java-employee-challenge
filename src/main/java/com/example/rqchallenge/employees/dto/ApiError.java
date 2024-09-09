package com.example.rqchallenge.employees.dto;


import com.example.rqchallenge.employees.constants.ApplicationConstants;
import lombok.Data;

@Data
public class ApiError {

    private String correlationId;
    private final String source;
    private String reasonCode;
    private String description;

    public ApiError() {
        this.source = ApplicationConstants.API_NAME;
    }

    public ApiError(String correlationId, String reasonCode, String description) {
        this.source = ApplicationConstants.API_NAME;
        this.correlationId = correlationId;
        this.reasonCode = reasonCode;
        this.description = description;
    }
}
