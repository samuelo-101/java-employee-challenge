package com.example.rqchallenge.employees.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestFilter extends OncePerRequestFilter {

    public static final String REQUEST_CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String requestCorrelationId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_CORRELATION_ID_KEY, requestCorrelationId);
        log.info("Request initiated {}: {}", REQUEST_CORRELATION_ID_KEY, requestCorrelationId);
        filterChain.doFilter(request, response);
        log.info("Request completed {}: {}", REQUEST_CORRELATION_ID_KEY, requestCorrelationId);
    }
}
