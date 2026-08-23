package com.dmart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary verification controller for validating Role-Based Access Control (RBAC).
 */
@RestController
@RequestMapping("/api/test")
public class TestSecurityController {

    @GetMapping("/customer")
    public ResponseEntity<Map<String, String>> customerAccess() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Access granted: CUSTOMER role verified.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff")
    public ResponseEntity<Map<String, String>> staffAccess() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Access granted: STAFF/ADMIN role verified.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, String>> adminAccess() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Access granted: ADMIN role verified.");
        return ResponseEntity.ok(response);
    }
}