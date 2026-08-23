package com.dmart;

import com.dmart.dto.request.AddToCartRequestDto;
import com.dmart.dto.request.UpdateCartItemRequestDto;
import com.dmart.entity.Cart;
import com.dmart.entity.Category;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CartIntegrationTests {

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

    private Category groceryCategory;
    private Product activeProduct;
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
                .name("Customer A")
                .email("custA@dmart.com")
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
                .name("Customer B")
                .email("custB@dmart.com")
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
                .email("staff@dmart.com")
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
                .email("admin@dmart.com")
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
        groceryCategory = categoryRepository.save(Category.builder()
                .name("Groceries")
                .description("Daily kitchen essentials")
                .active(true)
                .build());

        activeProduct = productRepository.save(Product.builder()
                .name("Basmati Rice 5kg")
                .description("Premium aged royal basmati rice")
                .price(new BigDecimal("450.00"))
                .stock(10)
                .category(groceryCategory)
                .active(true)
                .build());

        inactiveProduct = productRepository.save(Product.builder()
                .name("Out of Season Exotic Mango 1kg")
                .description("Alphonso mangoes")
                .price(new BigDecimal("300.00"))
                .stock(5)
                .category(groceryCategory)
                .active(false)
                .build());
    }

    // ========================================================
    // 1. AUTHENTICATION & ROLE AUTHORIZATION TESTS
    // ========================================================

    @Test
    void testGetCartWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCartWithCustomerTokenReturnsEmptyCart200() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(customerA.getId()))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0.00));
    }

    @Test
    void testStaffAndAdminAccessToCartReturns403() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // ========================================================
    // 2. ADD TO CART & DUPLICATE ACCUMULATION TESTS
    // ========================================================

    @Test
    void testAddToCartSuccessAndStockNotReduced() throws Exception {
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .productId(activeProduct.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(activeProduct.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].price").value(450.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(900.00))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalAmount").value(900.00));

        // Verify product stock in DB was NOT reduced
        Product freshProduct = productRepository.findById(activeProduct.getId()).orElseThrow();
        assertEquals(10, freshProduct.getStock());
    }

    @Test
    void testAddSameProductTwiceIncreasesQuantityWithoutDuplicates() throws Exception {
        AddToCartRequestDto firstAdd = AddToCartRequestDto.builder()
                .productId(activeProduct.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        // Add 3 more of same product
        AddToCartRequestDto secondAdd = AddToCartRequestDto.builder()
                .productId(activeProduct.getId())
                .quantity(3)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1))) // Still exactly 1 item
                .andExpect(jsonPath("$.items[0].quantity").value(5)) // 2 + 3 = 5
                .andExpect(jsonPath("$.items[0].subtotal").value(2250.00))
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.totalAmount").value(2250.00));
    }

    @Test
    void testAddInactiveProductReturns409() throws Exception {
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .productId(inactiveProduct.getId())
                .quantity(1)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Product is currently unavailable."));
    }

    @Test
    void testAddNonExistingProductReturns404() throws Exception {
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .productId(99999L)
                .quantity(1)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testAddQuantityGreaterThanStockReturns409() throws Exception {
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .productId(activeProduct.getId())
                .quantity(11) // Available is 10
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testAddZeroOrNegativeQuantityReturns400() throws Exception {
        String badZeroPayload = "{\"productId\": " + activeProduct.getId() + ", \"quantity\": 0}";
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badZeroPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        String badNegativePayload = "{\"productId\": " + activeProduct.getId() + ", \"quantity\": -5}";
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badNegativePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ========================================================
    // 3. UPDATE, REMOVE, CLEAR & ISOLATION TESTS
    // ========================================================

    @Test
    void testUpdateCartItemQuantitySuccess() throws Exception {
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .productId(activeProduct.getId())
                .quantity(2)
                .build();

        MvcResult result = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andReturn();

        long cartItemId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("items").get(0).get("id").asLong();

        // Update quantity to 4
        UpdateCartItemRequestDto updateDto = UpdateCartItemRequestDto.builder()
                .quantity(4)
                .build();

        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(4))
                .andExpect(jsonPath("$.items[0].subtotal").value(1800.00))
                .andExpect(jsonPath("$.totalItems").value(4))
                .andExpect(jsonPath("$.totalAmount").value(1800.00));
    }

    @Test
    void testCustomerCannotAccessOrModifyAnotherCustomersCart() throws Exception {
        // Customer A adds item
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .productId(activeProduct.getId())
                .quantity(2)
                .build();

        MvcResult resultA = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andReturn();

        long cartItemIdA = objectMapper.readTree(resultA.getResponse().getContentAsString())
                .get("items").get(0).get("id").asLong();

        // Customer B checks their cart -> should be empty
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.userId").value(customerB.getId()));

        // Customer B attempts to update Customer A's cart item -> 404 (or access denied)
        UpdateCartItemRequestDto updateDto = UpdateCartItemRequestDto.builder()
                .quantity(5)
                .build();

        mockMvc.perform(put("/api/cart/items/" + cartItemIdA)
                        .header("Authorization", "Bearer " + customerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        // Customer B attempts to delete Customer A's cart item -> 404
        mockMvc.perform(delete("/api/cart/items/" + cartItemIdA)
                        .header("Authorization", "Bearer " + customerBToken))
                .andExpect(status().isNotFound());

        // Customer A's item is still safe in their cart
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void testRemoveAndClearCart() throws Exception {
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .productId(activeProduct.getId())
                .quantity(3)
                .build();

        MvcResult result = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andReturn();

        long cartItemId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("items").get(0).get("id").asLong();

        // 1. Remove single item
        mockMvc.perform(delete("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0.00));

        // 2. Add item again and test clearCart
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        mockMvc.perform(delete("/api/cart")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0.00));
    }

    @Test
    void testPriceCalculationReflectsDatabasePriceChanges() throws Exception {
        // Customer adds product at price 450.00
        AddToCartRequestDto addDto = AddToCartRequestDto.builder()
                .productId(activeProduct.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].price").value(450.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(900.00));

        // Admin updates product price in DB to 500.00
        activeProduct.setPrice(new BigDecimal("500.00"));
        productRepository.save(activeProduct);

        // Next GET /api/cart calculates subtotal with new price (500.00 * 2 = 1000.00)
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].price").value(500.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(1000.00))
                .andExpect(jsonPath("$.totalAmount").value(1000.00));
    }
}