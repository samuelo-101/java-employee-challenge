package com.example.rqchallenge.employees.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalApiRedirectException extends RuntimeException {

    public ExternalApiRedirectException(String message) {
        super("Request redirected.");
        log.error(message);
    }
}
