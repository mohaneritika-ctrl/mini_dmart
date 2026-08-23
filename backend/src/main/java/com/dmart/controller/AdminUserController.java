package com.dmart.controller;

import com.dmart.entity.AuditLog;
import com.dmart.entity.Role;
import com.dmart.entity.User;
import com.dmart.repository.AuditLogRepository;
import com.dmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(@RequestParam(required = false) Role role) {
        List<User> users = (role != null)
                ? userRepository.findByRoleOrderByCreatedAtDesc(role)
                : userRepository.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> response = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("email", u.getEmail());
            map.put("phone", u.getPhone());
            map.put("role", u.getRole());
            map.put("active", u.getActive());
            map.put("lastLoginAt", u.getLastLoginAt());
            map.put("createdAt", u.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<Map<String, Object>>> getAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> response = logs.stream().map(l -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", l.getId());
            map.put("action", l.getAction());
            map.put("entityType", l.getEntityType());
            map.put("entityId", l.getEntityId());
            map.put("description", l.getDescription());
            map.put("createdAt", l.getCreatedAt());
            if (l.getUser() != null) {
                map.put("userId", l.getUser().getId());
                map.put("userName", l.getUser().getName());
                map.put("userEmail", l.getUser().getEmail());
                map.put("userRole", l.getUser().getRole());
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.findByRole(Role.CUSTOMER).size();
        long totalStaff = userRepository.findByRole(Role.STAFF).size();
        long totalAdmins = userRepository.findByRole(Role.ADMIN).size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalCustomers", totalCustomers);
        stats.put("totalStaff", totalStaff);
        stats.put("totalAdmins", totalAdmins);

        return ResponseEntity.ok(stats);
    }
}
