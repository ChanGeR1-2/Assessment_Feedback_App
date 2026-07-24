package com.dissertation.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;

@Component
public class SecurityErrorHandlers {

    private final JsonMapper jsonMapper;

    public SecurityErrorHandlers(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /** No or invalid credentials → 401 */
    public AuthenticationEntryPoint entryPoint() {
        return (request, response, ex) ->
                write(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
    }

    /** Authenticated but not permitted → 403 */
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) ->
                write(response, HttpServletResponse.SC_FORBIDDEN,
                        "You do not have permission to perform this action");
    }

    private void write(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), Map.of("message", message));
    }
}