package com.bank.onboarding.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Server is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Bank Onboarding API");
        response.put("version", "1.1.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to Bank Onboarding API");
        response.put("health", "/api/health");
        response.put("actuator", "/actuator/health");
        response.put("customers", "/api/customers");
        response.put("accounts", "/api/accounts");

        return ResponseEntity.ok(response);
    }
}
