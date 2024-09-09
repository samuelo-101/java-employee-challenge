package com.example.rqchallenge.employees.exception;

public class NoDataException extends RuntimeException {

    public NoDataException() {
        super("No data returned for the request.");
    }
}
