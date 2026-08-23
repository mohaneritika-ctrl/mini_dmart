package com.dmart;

import com.dmart.dto.request.AddToCartRequestDto;
import com.dmart.dto.request.CheckoutRequestDto;
import com.dmart.dto.request.UpdateOrderStatusRequestDto;
import com.dmart.entity.Category;
import com.dmart.entity.OrderStatus;
import com.dmart.entity.OrderType;
import com.dmart.entity.Product;
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
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTests {

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
    private Product oilProduct;
    private Product inactiveProduct;

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

        // 1. Create Customer A
        customerA = userRepository.save(User.builder()
                .name("Alice Customer")
                .email("alice.order@dmart.com")
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

        // 2. Create Customer B
        customerB = userRepository.save(User.builder()
                .name("Bob Customer")
                .email("bob.order@dmart.com")
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

        // 3. Create Staff
        staffUser = userRepository.save(User.builder()
                .name("Staff Member")
                .email("staff.order@dmart.com")
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

        // 4. Create Admin
        adminUser = userRepository.save(User.builder()
                .name("Admin Boss")
                .email("admin.order@dmart.com")
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

        // 5. Setup Category & Products
        pantryCategory = categoryRepository.save(Category.builder()
                .name("Pantry & Staples")
                .description("Rice, Oil, Sugar and Spices")
                .active(true)
                .build());

        riceProduct = productRepository.save(Product.builder()
                .name("Royal Basmati Rice 5kg")
                .description("Aged long grain aromatic rice")
                .price(new BigDecimal("450.00"))
                .stock(10)
                .category(pantryCategory)
                .active(true)
                .build());

        oilProduct = productRepository.save(Product.builder()
                .name("Sunflower Cooking Oil 1L")
                .description("Refined cooking oil")
                .price(new BigDecimal("140.00"))
                .stock(15)
                .category(pantryCategory)
                .active(true)
                .build());

        inactiveProduct = productRepository.save(Product.builder()
                .name("Discontinued Tea 500g")
                .description("Special blend tea")
                .price(new BigDecimal("220.00"))
                .stock(10)
                .category(pantryCategory)
                .active(false)
                .build());
    }

    // ========================================================
    // 1. AUTHENTICATION & AUTHORIZATION TESTS
    // ========================================================

    @Test
    void testCheckoutWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/orders/checkout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testStaffAndAdminCannotCheckoutReturns403() throws Exception {
        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCustomerCannotAccessStaffOrAdminEndpointsReturns403() throws Exception {
        mockMvc.perform(get("/api/staff/orders")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testStaffCannotAccessAdminOrdersReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    // ========================================================
    // 2. EMPTY CART CHECKOUT TEST
    // ========================================================

    @Test
    void testEmptyCartCheckoutFailsWith400() throws Exception {
        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, orderRepository.count());
    }

    // ========================================================
    // 3. SUCCESSFUL CHECKOUT & STOCK REDUCTION & CART CLEAR
    // ========================================================

    @Test
    void testSuccessfulCheckoutReducesStockAndClearsCart() throws Exception {
        // Alice adds Rice (qty 2) and Oil (qty 3) to cart
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddToCartRequestDto.builder().productId(riceProduct.getId()).quantity(2).build())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddToCartRequestDto.builder().productId(oilProduct.getId()).quantity(3).build())))
                .andExpect(status().isOk());

        CheckoutRequestDto checkoutDto = CheckoutRequestDto.builder()
                .orderType(OrderType.PICKUP)
                .pickupTimeSlot("10:00 AM - 12:00 PM")
                .build();

        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(customerA.getId()))
                .andExpect(jsonPath("$.orderStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(1320.00))
                .andExpect(jsonPath("$.items", hasSize(2)));

        // Verify product stocks in PostgreSQL reduced
        Product freshRice = productRepository.findById(riceProduct.getId()).orElseThrow();
        Product freshOil = productRepository.findById(oilProduct.getId()).orElseThrow();
        assertEquals(8, freshRice.getStock());  // was 10, ordered 2 -> 8
        assertEquals(12, freshOil.getStock()); // was 15, ordered 3 -> 12

        // Verify Alice's cart is now empty
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    // ========================================================
    // 4. CUSTOMER ORDER CANCELLATION & STOCK RESTORATION
    // ========================================================

    @Test
    void testCustomerCancelOrderRestoresStockSuccessfully() throws Exception {
        // Alice adds Rice (qty 3) and checkouts
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddToCartRequestDto.builder().productId(riceProduct.getId()).quantity(3).build())))
                .andExpect(status().isOk());

        MvcResult chkResult = mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isCreated())
                .andReturn();

        long orderId = objectMapper.readTree(chkResult.getResponse().getContentAsString()).get("id").asLong();

        // Stock became 10 - 3 = 7
        Product stockAfterOrder = productRepository.findById(riceProduct.getId()).orElseThrow();
        assertEquals(7, stockAfterOrder.getStock());

        // Alice cancels the order
        mockMvc.perform(put("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.orderStatus").value("CANCELLED"));

        // Stock restored back to 7 + 3 = 10
        Product stockAfterCancel = productRepository.findById(riceProduct.getId()).orElseThrow();
        assertEquals(10, stockAfterCancel.getStock());

        // Alice attempts to cancel the same order again -> 409 Conflict, stock must remain 10 (NOT 13)
        mockMvc.perform(put("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Order is already cancelled."));

        Product stockAfterSecondCancelAttempt = productRepository.findById(riceProduct.getId()).orElseThrow();
        assertEquals(10, stockAfterSecondCancelAttempt.getStock());
    }

    @Test
    void testCustomerCannotCancelCompletedOrderReturns409() throws Exception {
        // Alice places order
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddToCartRequestDto.builder().productId(riceProduct.getId()).quantity(2).build())))
                .andExpect(status().isOk());

        MvcResult chkResult = mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isCreated())
                .andReturn();

        long orderId = objectMapper.readTree(chkResult.getResponse().getContentAsString()).get("id").asLong();

        // Staff moves order through workflow to COMPLETED: CONFIRMED -> PREPARING -> READY_FOR_PICKUP -> COMPLETED
        mockMvc.perform(put("/api/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateOrderStatusRequestDto.builder().status(OrderStatus.PREPARING).build())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateOrderStatusRequestDto.builder().status(OrderStatus.READY_FOR_PICKUP).build())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateOrderStatusRequestDto.builder().status(OrderStatus.COMPLETED).build())))
                .andExpect(status().isOk());

        // Alice tries to cancel completed order -> 409 Conflict
        mockMvc.perform(put("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ========================================================
    // 5. STAFF & ADMIN ORDER MANAGEMENT & TRANSITIONS
    // ========================================================

    @Test
    void testStaffAndAdminOrderListAndStatusTransitions() throws Exception {
        // Alice places order
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddToCartRequestDto.builder().productId(riceProduct.getId()).quantity(1).build())))
                .andExpect(status().isOk());

        MvcResult chkResult = mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isCreated())
                .andReturn();

        long orderId = objectMapper.readTree(chkResult.getResponse().getContentAsString()).get("id").asLong();

        // 1. Staff lists all orders
        mockMvc.perform(get("/api/staff/orders")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(orderId));

        // 2. Staff filters by status
        mockMvc.perform(get("/api/staff/orders?status=CONFIRMED")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/staff/orders?status=COMPLETED")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 3. Staff attempts illegal status jump: CONFIRMED -> COMPLETED directly -> 409 Conflict
        mockMvc.perform(put("/api/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateOrderStatusRequestDto.builder().status(OrderStatus.COMPLETED).build())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // 4. Admin moves CONFIRMED -> PREPARING
        mockMvc.perform(put("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateOrderStatusRequestDto.builder().status(OrderStatus.PREPARING).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("PREPARING"));

        // 5. Admin lists orders
        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}