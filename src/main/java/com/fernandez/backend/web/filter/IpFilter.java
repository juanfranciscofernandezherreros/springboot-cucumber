package com.fernandez.backend.web.filter;

import com.fernandez.backend.application.port.in.IIpLockService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "application.security.ip-lock.enabled",
        havingValue = "true"
)
public class IpFilter extends OncePerRequestFilter {

    private final IIpLockService ipLockService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = request.getRemoteAddr();

        if (ipLockService.isIpBlocked(ip)) {
            response.setStatus(423); // Locked
            response.setContentType("text/plain");
            response.getWriter().write(
                    "Tu IP está bloqueada temporalmente por actividad sospechosa."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
