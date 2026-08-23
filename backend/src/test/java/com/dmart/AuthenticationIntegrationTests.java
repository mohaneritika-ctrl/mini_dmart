package com.dmart;

import com.dmart.dto.request.LoginRequest;
import com.dmart.dto.request.RegisterRequest;
import com.dmart.entity.Role;
import com.dmart.entity.User;
import com.dmart.repository.CartItemRepository;
import com.dmart.repository.CartRepository;
import com.dmart.repository.OrderItemRepository;
import com.dmart.repository.OrderRepository;
import com.dmart.repository.ReturnRequestRepository;
import com.dmart.repository.UserRepository;
import com.dmart.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        returnRequestRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCustomerRegistrationSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Ritik Customer")
                .email("customer@dmart.com")
                .password("Password@123")
                .phone("9876543210")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("customer@dmart.com"))
                .andExpect(jsonPath("$.name").value("Ritik Customer"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        Optional<User> savedUserOpt = userRepository.findByEmail("customer@dmart.com");
        assertTrue(savedUserOpt.isPresent());
        User savedUser = savedUserOpt.get();
        assertTrue(passwordEncoder.matches("Password@123", savedUser.getPassword()));
        assertTrue(savedUser.getPassword().startsWith("$") || savedUser.getPassword().startsWith("$"));
        assertFalse(savedUser.getPassword().contains("Password@123"));
    }

    @Test
    void testDuplicateEmailRegistrationFailsWith409() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("First User")
                .email("dup@dmart.com")
                .password("Password@123")
                .phone("9876543210")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempt second registration with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email is already registered: dup@dmart.com"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Login User")
                .email("login@dmart.com")
                .password("ValidPass123")
                .phone("9876543210")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("login@dmart.com")
                .password("ValidPass123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("login@dmart.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void testLoginInvalidCredentialsReturns401() throws Exception {
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Login User")
                .email("user1@dmart.com")
                .password("ValidPass123")
                .phone("9876543210")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest wrongPass = LoginRequest.builder()
                .email("user1@dmart.com")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPass)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testProtectedEndpointsWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/test/customer"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void testRbacPermissions() throws Exception {
        // 1. Create Customer and verify endpoints
        RegisterRequest custReq = RegisterRequest.builder()
                .name("Cust User")
                .email("cust@dmart.com")
                .password("CustPass123")
                .phone("9876543210")
                .build();

        MvcResult custResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(custReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String custToken = objectMapper.readTree(custResult.getResponse().getContentAsString()).get("token").asText();

        // Customer accesses customer endpoint -> 200 OK
        mockMvc.perform(get("/api/test/customer")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted: CUSTOMER role verified."));

        // Customer accesses staff endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/test/staff")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // Customer accesses admin endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // 2. Create Staff user directly in repository and generate token
        User staffUser = User.builder()
                .name("Staff Member")
                .email("staff@dmart.com")
                .password(passwordEncoder.encode("StaffPass123"))
                .role(Role.STAFF)
                .active(true)
                .build();
        User savedStaff = userRepository.save(staffUser);
        org.springframework.security.core.userdetails.User staffDetails =
                new org.springframework.security.core.userdetails.User(
                        savedStaff.getEmail(),
                        savedStaff.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"))
                );
        String staffToken = jwtService.generateToken(staffDetails, savedStaff.getId(), savedStaff.getRole());

        // Staff accesses staff endpoint -> 200 OK
        mockMvc.perform(get("/api/test/staff")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted: STAFF/ADMIN role verified."));

        // Staff accesses admin endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());

        // 3. Create Admin user directly in repository and generate token
        User adminUser = User.builder()
                .name("Admin Boss")
                .email("admin@dmart.com")
                .password(passwordEncoder.encode("AdminPass123"))
                .role(Role.ADMIN)
                .active(true)
                .build();
        User savedAdmin = userRepository.save(adminUser);
        org.springframework.security.core.userdetails.User adminDetails =
                new org.springframework.security.core.userdetails.User(
                        savedAdmin.getEmail(),
                        savedAdmin.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
        String adminToken = jwtService.generateToken(adminDetails, savedAdmin.getId(), savedAdmin.getRole());

        // Admin accesses admin endpoint -> 200 OK
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted: ADMIN role verified."));

        // Admin accesses staff endpoint -> 200 OK
        mockMvc.perform(get("/api/test/staff")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testCorsPreflightAndAllowedOriginForMultiplePorts() throws Exception {
        for (String origin : List.of("http://localhost:5173", "http://localhost:5175", "http://localhost:5177", "http://localhost:5179")) {
            // 1. Preflight OPTIONS request
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/products")
                            .header("Origin", origin)
                            .header("Access-Control-Request-Method", "GET")
                            .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", origin))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Access-Control-Allow-Credentials", "true"));

            // 2. Unauthenticated GET /api/products
            mockMvc.perform(get("/api/products")
                            .header("Origin", origin))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", origin))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Access-Control-Allow-Credentials", "true"));

            // 3. Unauthenticated GET /api/categories
            mockMvc.perform(get("/api/categories")
                            .header("Origin", origin))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", origin))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Access-Control-Allow-Credentials", "true"));
        }
    }
}