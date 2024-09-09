package com.example.rqchallenge.employees.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(final String id) {
        super("Could not find employee with id: " + id);
    }
}
