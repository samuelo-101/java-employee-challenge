package com.example.rqchallenge.employees.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmployeeApiInternalServerException extends RuntimeException {

    public EmployeeApiInternalServerException() {
        super("An error occurred and the request could not be processed.");
    }

    public EmployeeApiInternalServerException(String message) {
        super("An error occurred and the request could not be processed.");
        log.error(message);
    }
}
