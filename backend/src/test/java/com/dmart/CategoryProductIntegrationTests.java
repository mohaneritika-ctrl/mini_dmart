package com.dmart;

import com.dmart.dto.request.CategoryRequestDto;
import com.dmart.dto.request.ProductRequestDto;
import com.dmart.dto.request.UpdateStockRequestDto;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryProductIntegrationTests {

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

    private String customerToken;
    private String staffToken;
    private String adminToken;

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

        // 1. Create Customer
        User customer = User.builder()
                .name("Customer User")
                .email("cust@dmart.com")
                .password(passwordEncoder.encode("Cust@123"))
                .role(Role.CUSTOMER)
                .active(true)
                .build();
        User savedCust = userRepository.save(customer);
        customerToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        savedCust.getEmail(), savedCust.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                ),
                savedCust.getId(), savedCust.getRole()
        );

        // 2. Create Staff
        User staff = User.builder()
                .name("Staff User")
                .email("staff@dmart.com")
                .password(passwordEncoder.encode("Staff@123"))
                .role(Role.STAFF)
                .active(true)
                .build();
        User savedStaff = userRepository.save(staff);
        staffToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        savedStaff.getEmail(), savedStaff.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"))
                ),
                savedStaff.getId(), savedStaff.getRole()
        );

        // 3. Create Admin
        User admin = User.builder()
                .name("Admin User")
                .email("admin@dmart.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .active(true)
                .build();
        User savedAdmin = userRepository.save(admin);
        adminToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        savedAdmin.getEmail(), savedAdmin.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                ),
                savedAdmin.getId(), savedAdmin.getRole()
        );
    }

    // ==========================================
    // CATEGORY TESTS
    // ==========================================

    @Test
    void testCreateCategoryByAdminAndStaff() throws Exception {
        CategoryRequestDto dairyDto = CategoryRequestDto.builder()
                .name("Dairy & Bakery")
                .description("Milk, cheese, butter and bread")
                .active(true)
                .build();

        // 1. Admin creates category
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dairyDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Dairy & Bakery"));

        // 2. Staff creates category
        CategoryRequestDto snacksDto = CategoryRequestDto.builder()
                .name("Snacks & Beverages")
                .description("Chips, biscuits, and juices")
                .active(true)
                .build();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(snacksDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Snacks & Beverages"));
    }

    @Test
    void testCreateCategoryByCustomerFailsWith403() throws Exception {
        CategoryRequestDto dto = CategoryRequestDto.builder()
                .name("Fruits & Vegetables")
                .description("Fresh produce")
                .active(true)
                .build();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testDuplicateCategoryNameFailsWith409() throws Exception {
        Category category = Category.builder()
                .name("Beverages")
                .description("Drinks")
                .active(true)
                .build();
        categoryRepository.save(category);

        CategoryRequestDto dupDto = CategoryRequestDto.builder()
                .name("beverages") // Case-insensitive duplicate test
                .description("Another drinks description")
                .active(true)
                .build();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dupDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testUpdateCategoryByStaff() throws Exception {
        Category category = Category.builder()
                .name("Produce")
                .description("Fresh items")
                .active(true)
                .build();
        Category saved = categoryRepository.save(category);

        CategoryRequestDto updateDto = CategoryRequestDto.builder()
                .name("Organic Produce")
                .description("Fresh 100% organic farm vegetables")
                .active(true)
                .build();

        mockMvc.perform(put("/api/categories/" + saved.getId())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Organic Produce"))
                .andExpect(jsonPath("$.description").value("Fresh 100% organic farm vegetables"));
    }

    @Test
    void testDeleteCategoryByCustomerFailsWith403() throws Exception {
        Category category = Category.builder()
                .name("Personal Care")
                .active(true)
                .build();
        Category saved = categoryRepository.save(category);

        mockMvc.perform(delete("/api/categories/" + saved.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteCategoryWithAssociatedProductsFailsWith409() throws Exception {
        Category category = Category.builder()
                .name("Pantry")
                .active(true)
                .build();
        Category savedCat = categoryRepository.save(category);

        Product product = Product.builder()
                .name("Basmati Rice 5kg")
                .price(new BigDecimal("499.00"))
                .stock(20)
                .active(true)
                .category(savedCat)
                .build();
        productRepository.save(product);

        // Attempt delete as Admin
        mockMvc.perform(delete("/api/categories/" + savedCat.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot delete category 'Pantry' (ID: " + savedCat.getId() + ") because it contains associated products. Consider deactivating the category instead."));
    }

    @Test
    void testDeleteCategoryByAdminSuccess() throws Exception {
        Category category = Category.builder()
                .name("Stationery")
                .active(true)
                .build();
        Category saved = categoryRepository.save(category);

        mockMvc.perform(delete("/api/categories/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        assertFalse(categoryRepository.existsById(saved.getId()));
    }

    // ==========================================
    // PRODUCT TESTS
    // ==========================================

    @Test
    void testCreateProductByAdminAndStaff() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Dairy").active(true).build());

        ProductRequestDto milkDto = ProductRequestDto.builder()
                .name("Full Cream Fresh Milk 1L")
                .description("Fresh pasteurized cow milk")
                .price(new BigDecimal("68.00"))
                .stock(50)
                .categoryId(category.getId())
                .active(true)
                .build();

        // 1. Staff creates product
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Full Cream Fresh Milk 1L"))
                .andExpect(jsonPath("$.price").value(68.00))
                .andExpect(jsonPath("$.categoryName").value("Dairy"));

        // 2. Customer attempts to create product -> 403
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(milkDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testProductValidationFailsOnNegativePriceOrStock() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Grains").active(true).build());

        ProductRequestDto badDto = ProductRequestDto.builder()
                .name("Wheat Flour 10kg")
                .price(new BigDecimal("-10.00")) // Invalid
                .stock(-5) // Invalid
                .categoryId(category.getId())
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.price").isNotEmpty())
                .andExpect(jsonPath("$.validationErrors.stock").isNotEmpty());
    }

    @Test
    void testSearchAndCategoryFilterCaseInsensitive() throws Exception {
        Category dairy = categoryRepository.save(Category.builder().name("Dairy").active(true).build());
        Category snacks = categoryRepository.save(Category.builder().name("Snacks").active(true).build());

        productRepository.save(Product.builder().name("Fresh Cow Milk 1L").price(new BigDecimal("60.00")).stock(30).active(true).category(dairy).build());
        productRepository.save(Product.builder().name("Skimmed MILK 500ml").price(new BigDecimal("35.00")).stock(20).active(true).category(dairy).build());
        productRepository.save(Product.builder().name("Milk Chocolate Bar").price(new BigDecimal("50.00")).stock(15).active(true).category(snacks).build());
        productRepository.save(Product.builder().name("Potato Chips").price(new BigDecimal("20.00")).stock(50).active(true).category(snacks).build());

        // 1. Search for "milk" (case-insensitive)
        mockMvc.perform(get("/api/products/search?name=milk")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));

        // 2. Filter by Dairy category
        mockMvc.perform(get("/api/products/category/" + dairy.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testStockManagement() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Beverages").active(true).build());
        Product product = productRepository.save(Product.builder()
                .name("Orange Juice 1L")
                .price(new BigDecimal("99.00"))
                .stock(10)
                .active(true)
                .category(category)
                .build());

        // 1. Staff updates stock
        UpdateStockRequestDto updateStock = UpdateStockRequestDto.builder().stock(75).build();

        mockMvc.perform(patch("/api/products/" + product.getId() + "/stock")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(75));

        // 2. Customer tries to update stock -> 403
        mockMvc.perform(patch("/api/products/" + product.getId() + "/stock")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStock)))
                .andExpect(status().isForbidden());

        // 3. Negative stock update -> 400
        UpdateStockRequestDto badStock = UpdateStockRequestDto.builder().stock(-10).build();
        mockMvc.perform(patch("/api/products/" + product.getId() + "/stock")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badStock)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testProductActivationAndCustomerVisibility() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Bakery").active(true).build());
        Product activeProduct = productRepository.save(Product.builder()
                .name("Whole Wheat Bread")
                .price(new BigDecimal("45.00"))
                .stock(20)
                .active(true)
                .category(category)
                .build());

        Product inactiveProduct = productRepository.save(Product.builder()
                .name("Seasonal Fruit Cake")
                .price(new BigDecimal("250.00"))
                .stock(5)
                .active(false) // Inactive product
                .category(category)
                .build());

        // 1. Customer listing only returns active products
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Whole Wheat Bread"));

        // 2. Customer fetching inactive product directly returns 404
        mockMvc.perform(get("/api/products/" + inactiveProduct.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // 3. Staff can view inactive product
        mockMvc.perform(get("/api/products/" + inactiveProduct.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Seasonal Fruit Cake"))
                .andExpect(jsonPath("$.active").value(false));

        // 4. Staff activates product
        mockMvc.perform(patch("/api/products/" + inactiveProduct.getId() + "/activate")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // 5. Customer can now view it
        mockMvc.perform(get("/api/products/" + inactiveProduct.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Seasonal Fruit Cake"));
    }
}