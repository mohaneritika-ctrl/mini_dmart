package com.dmart;

import com.dmart.dto.request.CreateReturnRequestDto;
import com.dmart.dto.request.UpdateReturnStatusDto;
import com.dmart.entity.Category;
import com.dmart.entity.Order;
import com.dmart.entity.OrderItem;
import com.dmart.entity.OrderStatus;
import com.dmart.entity.OrderType;
import com.dmart.entity.Product;
import com.dmart.entity.ReturnRequest;
import com.dmart.entity.ReturnStatus;
import com.dmart.entity.ReturnType;
import com.dmart.entity.Role;
import com.dmart.entity.User;
import com.dmart.repository.CartItemRepository;
import com.dmart.repository.CartRepository;
import com.dmart.repository.CategoryRepository;
import com.dmart.repository.OrderItemRepository;
import com.dmart.repository.OrderRepository;
import com.dmart.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReturnIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

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

    private User customerA;
    private User customerB;
    private User staffUser;
    private User adminUser;

    private String customerAToken;
    private String customerBToken;
    private String staffToken;
    private String adminToken;

    private Category pantryCategory;
    private Product riceProduct;
    private Order completedOrderAlice;
    private OrderItem aliceRiceItem;

    @BeforeEach
    void setUp() {
        returnRequestRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Users
        customerA = userRepository.save(User.builder()
                .name("Alice Return")
                .email("alice.return@dmart.com")
                .password(passwordEncoder.encode("Pass@123"))
                .role(Role.CUSTOMER)
                .active(true)
                .build());
        customerAToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        customerA.getEmail(), customerA.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                ),
                customerA.getId(), customerA.getRole()
        );

        customerB = userRepository.save(User.builder()
                .name("Bob Return")
                .email("bob.return@dmart.com")
                .password(passwordEncoder.encode("Pass@123"))
                .role(Role.CUSTOMER)
                .active(true)
                .build());
        customerBToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        customerB.getEmail(), customerB.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                ),
                customerB.getId(), customerB.getRole()
        );

        staffUser = userRepository.save(User.builder()
                .name("Staff Return")
                .email("staff.return@dmart.com")
                .password(passwordEncoder.encode("Pass@123"))
                .role(Role.STAFF)
                .active(true)
                .build());
        staffToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        staffUser.getEmail(), staffUser.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"))
                ),
                staffUser.getId(), staffUser.getRole()
        );

        adminUser = userRepository.save(User.builder()
                .name("Admin Return")
                .email("admin.return@dmart.com")
                .password(passwordEncoder.encode("Pass@123"))
                .role(Role.ADMIN)
                .active(true)
                .build());
        adminToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        adminUser.getEmail(), adminUser.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                ),
                adminUser.getId(), adminUser.getRole()
        );

        // 2. Setup Category & Product
        pantryCategory = categoryRepository.save(Category.builder()
                .name("Pantry Staples")
                .description("Rice and grains")
                .active(true)
                .build());

        riceProduct = productRepository.save(Product.builder()
                .name("Premium Basmati 5kg")
                .description("Long grain aged rice")
                .price(new BigDecimal("500.00"))
                .stock(20)
                .category(pantryCategory)
                .active(true)
                .build());

        // 3. Setup Completed Order for Alice (Delivered 2 days ago)
        completedOrderAlice = orderRepository.save(Order.builder()
                .user(customerA)
                .orderType(OrderType.DELIVERY)
                .orderStatus(OrderStatus.COMPLETED)
                .totalAmount(new BigDecimal("1500.00"))
                .deliveryAddress("123 Main St")
                .build());

        aliceRiceItem = orderItemRepository.save(OrderItem.builder()
                .order(completedOrderAlice)
                .product(riceProduct)
                .quantity(3)
                .price(new BigDecimal("500.00"))
                .build());
    }

    // ========================================================
    // 1. CUSTOMER RETURN REQUEST CREATION & ELIGIBILITY
    // ========================================================

    @Test
    void testCreateReturnRequestSuccessfully() throws Exception {
        CreateReturnRequestDto dto = CreateReturnRequestDto.builder()
                .orderId(completedOrderAlice.getId())
                .orderItemId(aliceRiceItem.getId())
                .type(ReturnType.RETURN)
                .quantity(2)
                .reason("Damaged Product")
                .note("Package torn on arrival")
                .build();

        mockMvc.perform(post("/api/returns")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.orderId").value(completedOrderAlice.getId()))
                .andExpect(jsonPath("$.productName").value("Premium Basmati 5kg"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.type").value("RETURN"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        // Verify stock remains untouched at creation time
        Product freshRice = productRepository.findById(riceProduct.getId()).orElseThrow();
        assertEquals(20, freshRice.getStock());
    }

    @Test
    void testCannotCreateReturnOnNonCompletedOrderReturns409() throws Exception {
        // Create an uncompleted CONFIRMED order
        Order confirmedOrder = orderRepository.save(Order.builder()
                .user(customerA)
                .orderType(OrderType.PICKUP)
                .orderStatus(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("500.00"))
                .build());

        OrderItem confirmedItem = orderItemRepository.save(OrderItem.builder()
                .order(confirmedOrder)
                .product(riceProduct)
                .quantity(1)
                .price(new BigDecimal("500.00"))
                .build());

        CreateReturnRequestDto dto = CreateReturnRequestDto.builder()
                .orderId(confirmedOrder.getId())
                .orderItemId(confirmedItem.getId())
                .type(ReturnType.RETURN)
                .quantity(1)
                .reason("Wrong Product")
                .build();

        mockMvc.perform(post("/api/returns")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Only completed/delivered orders are eligible for return or exchange."));
    }

    @Test
    void testCannotReturnMoreThanPurchasedQuantityReturns409() throws Exception {
        CreateReturnRequestDto dto = CreateReturnRequestDto.builder()
                .orderId(completedOrderAlice.getId())
                .orderItemId(aliceRiceItem.getId())
                .type(ReturnType.RETURN)
                .quantity(5) // purchased only 3
                .reason("Expired Product")
                .build();

        mockMvc.perform(post("/api/returns")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testDuplicateActiveRequestForSameItemReturns409() throws Exception {
        CreateReturnRequestDto dto = CreateReturnRequestDto.builder()
                .orderId(completedOrderAlice.getId())
                .orderItemId(aliceRiceItem.getId())
                .type(ReturnType.RETURN)
                .quantity(1)
                .reason("Damaged Product")
                .build();

        // 1st request succeeds
        mockMvc.perform(post("/api/returns")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // 2nd request for same item rejected
        mockMvc.perform(post("/api/returns")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An active return or exchange request already exists for this order item."));
    }

    // ========================================================
    // 2. IDOR & SECURITY CHECKS
    // ========================================================

    @Test
    void testBobCannotReturnAlicesOrderReturns404() throws Exception {
        CreateReturnRequestDto dto = CreateReturnRequestDto.builder()
                .orderId(completedOrderAlice.getId())
                .orderItemId(aliceRiceItem.getId())
                .type(ReturnType.RETURN)
                .quantity(1)
                .reason("Wrong Product")
                .build();

        // Bob tries to return Alice's order
        mockMvc.perform(post("/api/returns")
                        .header("Authorization", "Bearer " + customerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testCustomerCannotAccessStaffOrAdminReturnEndpointsReturns403() throws Exception {
        mockMvc.perform(get("/api/staff/returns")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/returns")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isForbidden());
    }

    // ========================================================
    // 3. STAFF & ADMIN WORKFLOW & INVENTORY MANAGEMENT
    // ========================================================

    @Test
    void testStaffApproveAndCompleteReturnRestoresInventory() throws Exception {
        // 1. Alice creates return request for 2 units
        ReturnRequest req = returnRequestRepository.save(ReturnRequest.builder()
                .order(completedOrderAlice)
                .orderItem(aliceRiceItem)
                .user(customerA)
                .type(ReturnType.RETURN)
                .quantity(2)
                .reason("Damaged Item")
                .status(ReturnStatus.REQUESTED)
                .build());

        // 2. Staff approves request
        mockMvc.perform(put("/api/staff/returns/" + req.getId() + "/approve")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Stock still untouched at APPROVED stage
        Product stockAtApproved = productRepository.findById(riceProduct.getId()).orElseThrow();
        assertEquals(20, stockAtApproved.getStock());

        // 3. Staff completes request -> stock restored (+2)
        mockMvc.perform(put("/api/staff/returns/" + req.getId() + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        Product stockAfterComplete = productRepository.findById(riceProduct.getId()).orElseThrow();
        assertEquals(22, stockAfterComplete.getStock()); // 20 + 2 = 22!
    }

    @Test
    void testStaffApproveAndCompleteExchangeDeductsReplacementStock() throws Exception {
        // Alice creates exchange request for 1 unit
        ReturnRequest req = returnRequestRepository.save(ReturnRequest.builder()
                .order(completedOrderAlice)
                .orderItem(aliceRiceItem)
                .user(customerA)
                .type(ReturnType.EXCHANGE)
                .quantity(1)
                .reason("Wrong Size")
                .status(ReturnStatus.REQUESTED)
                .build());

        // Staff approves
        mockMvc.perform(put("/api/staff/returns/" + req.getId() + "/approve")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Staff completes exchange -> replacement deducted (-1 from 20 -> 19)
        mockMvc.perform(put("/api/staff/returns/" + req.getId() + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        Product stockAfterExchange = productRepository.findById(riceProduct.getId()).orElseThrow();
        assertEquals(19, stockAfterExchange.getStock()); // 20 - 1 = 19
    }

    @Test
    void testStaffRejectionDoesNotTouchStock() throws Exception {
        ReturnRequest req = returnRequestRepository.save(ReturnRequest.builder()
                .order(completedOrderAlice)
                .orderItem(aliceRiceItem)
                .user(customerA)
                .type(ReturnType.RETURN)
                .quantity(1)
                .reason("No Longer Needed")
                .status(ReturnStatus.REQUESTED)
                .build());

        UpdateReturnStatusDto rejectDto = UpdateReturnStatusDto.builder()
                .status(ReturnStatus.REJECTED)
                .staffComment("Item opened and used, ineligible for return")
                .build();

        mockMvc.perform(put("/api/staff/returns/" + req.getId() + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.staffComment").value("Item opened and used, ineligible for return"));

        Product stockAfterReject = productRepository.findById(riceProduct.getId()).orElseThrow();
        assertEquals(20, stockAfterReject.getStock()); // unchanged
    }

    @Test
    void testAdminViewsAndManagesReturns() throws Exception {
        returnRequestRepository.save(ReturnRequest.builder()
                .order(completedOrderAlice)
                .orderItem(aliceRiceItem)
                .user(customerA)
                .type(ReturnType.RETURN)
                .quantity(1)
                .reason("Product Expired")
                .status(ReturnStatus.REQUESTED)
                .build());

        // Admin lists all requests
        mockMvc.perform(get("/api/admin/returns")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}