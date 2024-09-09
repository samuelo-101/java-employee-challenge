package com.example.rqchallenge.employees.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalApiInternalServerException extends RuntimeException {

    public ExternalApiInternalServerException() {
        super("An error occurred and the request could not be processed.");
    }
    public ExternalApiInternalServerException(String message) {
        super("An error occurred and the request could not be processed.");
        log.error(message);
    }
}
