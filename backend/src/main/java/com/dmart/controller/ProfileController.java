package com.dmart.controller;

import com.dmart.entity.User;
import com.dmart.repository.OrderRepository;
import com.dmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("role", user.getRole());
        profile.put("active", user.getActive());
        profile.put("lastLoginAt", user.getLastLoginAt());
        profile.put("createdAt", user.getCreatedAt());

        if (user.getRole().name().equals("CUSTOMER")) {
            long totalOrders = orderRepository.countByUserId(user.getId());
            profile.put("totalOrdersPlaced", totalOrders);
        } else if (user.getRole().name().equals("STAFF")) {
            long totalOrdersInStore = orderRepository.count();
            profile.put("totalStoreOrders", totalOrdersInStore);
            profile.put("assignedDepartment", "Fulfillment & Dispatch");
            profile.put("shiftStatus", "ON_DUTY");
        }

        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (body.containsKey("name") && body.get("name") != null && !body.get("name").isBlank()) {
            user.setName(body.get("name").trim());
        }
        if (body.containsKey("phone")) {
            user.setPhone(body.get("phone") != null ? body.get("phone").trim() : null);
        }

        User updated = userRepository.save(user);

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", updated.getId());
        profile.put("name", updated.getName());
        profile.put("email", updated.getEmail());
        profile.put("phone", updated.getPhone());
        profile.put("role", updated.getRole());
        profile.put("lastLoginAt", updated.getLastLoginAt());

        return ResponseEntity.ok(profile);
    }
}
