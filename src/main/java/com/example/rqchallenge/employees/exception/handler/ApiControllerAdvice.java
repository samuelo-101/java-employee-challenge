package com.example.rqchallenge.employees.exception.handler;

import com.example.rqchallenge.employees.constants.ErrorCodeConstants;
import com.example.rqchallenge.employees.dto.ApiError;
import com.example.rqchallenge.employees.exception.*;
import com.example.rqchallenge.employees.filter.RequestFilter;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(EmployeeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> employeeNotFoundErrorHandler(EmployeeNotFoundException ex) {
        final String requestCorrelationId = getRequestCorrelationId();
        log.error("Error {}: {} | {}", RequestFilter.REQUEST_CORRELATION_ID_KEY, requestCorrelationId, ex.getMessage());
        return errorFormatter(
                HttpStatus.NOT_FOUND,
                new ApiError(requestCorrelationId, ErrorCodeConstants.EMPLOYEE_NOT_FOUND_CODE, ex.getMessage())
        );
    }

    @ExceptionHandler(NoDataException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> noDataErrorHandler(NoDataException ex) {
        final String requestCorrelationId = getRequestCorrelationId();
        log.error("Error {}: {} | {}", RequestFilter.REQUEST_CORRELATION_ID_KEY, requestCorrelationId, ex.getMessage());
        return errorFormatter(
                HttpStatus.NOT_FOUND,
                new ApiError(requestCorrelationId, ErrorCodeConstants.EMPLOYEE_NO_DATA, ex.getMessage())
        );
    }

    @ExceptionHandler(ExternalApiRedirectException.class)
    @ResponseStatus(HttpStatus.TEMPORARY_REDIRECT)
    public ResponseEntity<ApiError> redirectErrorHandler(ExternalApiRedirectException ex) {
        final String requestCorrelationId = getRequestCorrelationId();
        log.error("Error {}: {} | {}", RequestFilter.REQUEST_CORRELATION_ID_KEY, requestCorrelationId, ex.getMessage());
        return errorFormatter(
                HttpStatus.TEMPORARY_REDIRECT,
                new ApiError(requestCorrelationId, ErrorCodeConstants.EXTERNAL_API_REDIRECT, ex.getMessage())
        );
    }

    @ExceptionHandler(EmployeeApiInternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiError> internalServerErrorHandler(EmployeeApiInternalServerException ex) {
        final String requestCorrelationId = getRequestCorrelationId();
        log.error("Error {}: {} | {}", RequestFilter.REQUEST_CORRELATION_ID_KEY, requestCorrelationId, ex.getMessage());
        return errorFormatter(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ApiError(requestCorrelationId, ErrorCodeConstants.EXTERNAL_API_SERVER_ERROR, ex.getMessage())
        );
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiError> invalidFieldErrorHandler(BadRequestException ex) {
        final String requestCorrelationId = getRequestCorrelationId();
        log.error("Error {}: {} | {}", RequestFilter.REQUEST_CORRELATION_ID_KEY, requestCorrelationId, ex.getMessage());
        return errorFormatter(
                HttpStatus.BAD_REQUEST,
                new ApiError(requestCorrelationId, ErrorCodeConstants.EXTERNAL_API_INVALID_FIELD, ex.getMessage())
        );
    }

    @ExceptionHandler(WebClientResponseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiError> invalidFieldErrorHandler(WebClientResponseException ex) {
        final String requestCorrelationId = getRequestCorrelationId();
        log.error("Error {}: {} | {}", RequestFilter.REQUEST_CORRELATION_ID_KEY, requestCorrelationId, ex.getMessage());
        return errorFormatter(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ApiError(requestCorrelationId, ErrorCodeConstants.EXTERNAL_API_SERVER_ERROR, String.format("Employees API returned error code %d.", ex.getStatusCode().value()))
        );
    }

    @ExceptionHandler(ReadTimeoutException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiError> apiTimeoutErrorHandler(ReadTimeoutException ex) {
        final String requestCorrelationId = getRequestCorrelationId();
        log.error("Error {}: {} | {}", RequestFilter.REQUEST_CORRELATION_ID_KEY, requestCorrelationId, ex.getMessage());
        return errorFormatter(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ApiError(requestCorrelationId, ErrorCodeConstants.EXTERNAL_API_SERVER_ERROR, "Employees API timed out. Please try again.")
        );
    }

    private ResponseEntity<ApiError> errorFormatter(HttpStatus status, ApiError apiError) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        return ResponseEntity.status(status).headers(responseHeaders).body(apiError);
    }

    private String getRequestCorrelationId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return (String) request.getAttribute(RequestFilter.REQUEST_CORRELATION_ID_KEY);
    }
}
