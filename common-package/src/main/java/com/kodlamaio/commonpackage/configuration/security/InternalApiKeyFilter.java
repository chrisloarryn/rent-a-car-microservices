package com.kodlamaio.commonpackage.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodlamaio.commonpackage.utils.constants.ExceptionTypes;
import com.kodlamaio.commonpackage.utils.results.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter
{
    private final ApplicationSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
    {
        return !request.getRequestURI().startsWith("/api/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException
    {
        String headerName = securityProperties.getInternalApiKeyHeader();
        String actualHeader = request.getHeader(headerName);
        String expectedHeader = securityProperties.getInternalApiKey();

        if (StringUtils.hasText(actualHeader) && actualHeader.equals(expectedHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ExceptionTypes.Exception.Authorization,
                "Internal API key is invalid or missing.",
                Map.of(headerName, "A valid internal API key is required."),
                request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
