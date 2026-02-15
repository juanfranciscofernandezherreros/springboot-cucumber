package com.fernandez.backend.web.filter;

import com.fernandez.backend.shared.constants.ServiceStrings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info(ServiceStrings.Filter.LOG_REQUEST, request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
        log.info(ServiceStrings.Filter.LOG_RESPONSE, response.getStatus(), request.getMethod(), request.getRequestURI());
    }
}
