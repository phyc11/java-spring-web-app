package com.example.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;

@RestController
public class GatewayProxyController {

    private final RestTemplate restTemplate;

    @Value("${auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${task-service.url:http://localhost:8082}")
    private String taskServiceUrl;

    @Value("${audit-service.url:http://localhost:8083}")
    private String auditServiceUrl;

    @Value("${analytics-service.url:http://localhost:8084}")
    private String analyticsServiceUrl;

    public GatewayProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping({"/api/auth/**", "/api/tasks/**", "/api/categories/**", "/api/audit-logs/**", "/api/analytics/**", "/api/export/**"})
    public ResponseEntity<byte[]> proxyRequest(@RequestBody(required = false) byte[] body,
                                              HttpMethod method,
                                              HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestPath += "?" + queryString;
        }

        String targetBaseUrl;
        if (requestPath.startsWith("/api/auth")) {
            targetBaseUrl = authServiceUrl;
        } else if (requestPath.startsWith("/api/tasks") || requestPath.startsWith("/api/categories")) {
            targetBaseUrl = taskServiceUrl;
        } else if (requestPath.startsWith("/api/audit-logs")) {
            targetBaseUrl = auditServiceUrl;
        } else {
            targetBaseUrl = analyticsServiceUrl;
        }

        String targetUrl = targetBaseUrl + requestPath;

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            headers.add(headerName, request.getHeader(headerName));
        });

        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(URI.create(targetUrl), method, httpEntity, byte[].class);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(("{\"success\":false,\"message\":\"Service Unavailable: " + ex.getMessage() + "\"}").getBytes());
        }
    }
}
