package com.dmart.config;

import com.dmart.entity.Category;
import com.dmart.entity.Product;
import com.dmart.entity.Role;
import com.dmart.entity.User;
import com.dmart.repository.CategoryRepository;
import com.dmart.repository.ProductRepository;
import com.dmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and seeding users, grocery categories, and products...");

        // 0. Seed Users
        seedUserIfNotFound("customer@dmart.com", "Customer User", "customer123", Role.CUSTOMER, "9876543210");
        seedUserIfNotFound("staff@dmart.com", "Staff User", "staff123", Role.STAFF, "9876543211");
        seedUserIfNotFound("admin@dmart.com", "Admin User", "admin123", Role.ADMIN, "9876543212");

        // 1. Seed Categories
        Map<String, Category> categories = new HashMap<>();
        String[][] categoryData = {
                {"Fruits", "Fresh seasonal and exotic organic fruits"},
                {"Vegetables", "Farm fresh daily vegetables, greens, and root crops"},
                {"Dairy", "Fresh milk, butter, cheese, paneer, and curd"},
                {"Beverages", "Tea, coffee, fruit juices, and cold beverages"},
                {"Snacks", "Biscuits, savory namkeen, chips, and chocolates"},
                {"Bakery", "Freshly baked artisan breads, cookies, and buns"},
                {"Personal Care", "Soaps, oral care, body wash, and hair care"},
                {"Household", "Detergents, cleaners, dishwash, and home essentials"},
                {"Pantry Staples", "Flour, rice, pulses, cooking oils, and daily spices"}
        };

        for (String[] cat : categoryData) {
            String name = cat[0];
            String desc = cat[1];
            Category category = categoryRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> categoryRepository.save(
                            Category.builder()
                                    .name(name)
                                    .description(desc)
                                    .active(true)
                                    .build()
                    ));
            categories.put(name, category);
        }

        // 2. Seed Products
        Object[][] products = {
                // Pantry Staples
                {"Premium Basmati 5kg", "Pantry Staples", "500.00", 20, "Long grain aromatic aged Indian Basmati rice.", "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80"},
                {"Aashirvaad Superior MP Atta (10kg)", "Pantry Staples", "440.00", 35, "Superior quality 100% pure whole wheat stone ground chakki fresh atta.", "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?auto=format&fit=crop&w=600&q=80"},
                {"Fortune Sunlite Sunflower Oil (1L)", "Pantry Staples", "145.00", 40, "Light and healthy refined sunflower cooking oil enriched with vitamins.", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=600&q=80"},
                {"Tata Salt Vacuum Evaporated (1kg)", "Pantry Staples", "28.00", 60, "Desh ka Namak, pure vacuum evaporated iodized cooking salt.", "https://images.unsplash.com/photo-1518110925495-5fe2fda0442c?auto=format&fit=crop&w=600&q=80"},
                {"Tata Sampann Toor Dal (1kg)", "Pantry Staples", "175.00", 45, "Natural, unpolished, high-protein Arhar / Toor Dal with rich wholesome taste.", "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?auto=format&fit=crop&w=600&q=80"},
                {"Everest Royal Garam Masala (100g)", "Pantry Staples", "88.00", 40, "Aromatic blend of roasted exotic Indian spices for authentic curries.", "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=600&q=80"},
                {"Catch Red Chilli Powder (200g)", "Pantry Staples", "92.00", 45, "Low temperature ground vibrant red spicy and flavorful Indian chilli powder.", "https://images.unsplash.com/photo-1627042633145-b780d842ba45?auto=format&fit=crop&w=600&q=80"},
                {"MDH Chunky Chat Masala (100g)", "Pantry Staples", "75.00", 40, "Zesty tangy seasoning spice blend for salads, snacks, and street food.", "https://images.unsplash.com/photo-1509358271058-acd22cc93898?auto=format&fit=crop&w=600&q=80"},
                {"Dabur 100% Pure Honey Squeezy (400g)", "Pantry Staples", "199.00", 35, "Rich pure natural golden honey for daily immunity and wellness.", "https://images.unsplash.com/photo-1587049352846-4a222e784d38?auto=format&fit=crop&w=600&q=80"},

                // Fruits
                {"Fresh Alphonso Mangoes (1kg)", "Fruits", "299.00", 40, "Sweet and juicy premium Ratnagiri Alphonso mangoes.", "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80"},
                {"Shimla Royal Apple (1kg)", "Fruits", "180.00", 50, "Crisp, sweet, and freshly harvested red Shimla apples.", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80"},
                {"Robusta Bananas (1 Dozen)", "Fruits", "60.00", 60, "Naturally ripened, potassium-rich fresh yellow bananas.", "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=600&q=80"},

                // Vegetables
                {"Farm Fresh Potatoes (2kg)", "Vegetables", "70.00", 80, "Grade-A unwashed farm fresh local potatoes for daily cooking.", "https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=600&q=80"},
                {"Hybrid Red Tomatoes (1kg)", "Vegetables", "45.00", 50, "Firm, juicy, farm-picked red ripe cooking tomatoes.", "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=600&q=80"},
                {"Fresh Red Onions (2kg)", "Vegetables", "85.00", 70, "Pungent, dry, and quality sorted red onions.", "https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=600&q=80"},

                // Dairy
                {"Amul Taaza Toned Milk (1L)", "Dairy", "54.00", 45, "Pasteurized homogenized toned milk rich in calcium and protein.", "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=600&q=80"},
                {"Amul Pasteurized Butter (500g)", "Dairy", "275.00", 35, "Delicious, creamy salted butter made from fresh pure milk cream.", "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?auto=format&fit=crop&w=600&q=80"},
                {"Fresh Malai Paneer (200g)", "Dairy", "95.00", 30, "Soft, tender, high-protein cottage cheese cubes.", "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=600&q=80"},

                // Beverages
                {"Tata Tea Gold Leaf Tea (500g)", "Beverages", "310.00", 40, "Rich blend of fine Assam tea leaves with gently rolled long leaves.", "https://images.unsplash.com/photo-1576092768241-dec231879fc3?auto=format&fit=crop&w=600&q=80"},
                {"Brooke Bond Red Label Tea (1kg)", "Beverages", "490.00", 35, "Taste of togetherness, blended with quality Assam CTC black tea leaves.", "https://images.unsplash.com/photo-1594631252845-29fc4cc8cde9?auto=format&fit=crop&w=600&q=80"},
                {"Nescafe Classic Instant Coffee (100g Jar)", "Beverages", "340.00", 30, "100% pure natural coffee made from roasted Robusta beans.", "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?auto=format&fit=crop&w=600&q=80"},
                {"Real Active 100% Mixed Fruit Juice (1L)", "Beverages", "130.00", 25, "No added sugar, natural blended juice from premium real fruits.", "https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=600&q=80"},

                // Snacks
                {"Maggi 2-Minute Noodles (12-Pack)", "Snacks", "168.00", 50, "India's favorite instant masala noodles made with goodness of iron.", "https://images.unsplash.com/photo-1612927601601-6638404737ce?auto=format&fit=crop&w=600&q=80"},
                {"Haldiram's Bhujia Sev (400g)", "Snacks", "115.00", 50, "Crispy, spiced moth bean flour crunchy savory Indian snack.", "https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80"},
                {"Lay's India's Magic Masala Chips (115g)", "Snacks", "50.00", 60, "Crispy ridged potato chips seasoned with authentic Indian spices.", "https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=600&q=80"},
                {"Cadbury Dairy Milk Silk Chocolate (150g)", "Snacks", "175.00", 40, "Smoother, creamier, and richer melt-in-mouth milk chocolate.", "https://images.unsplash.com/photo-1549007994-cb92caebd54b?auto=format&fit=crop&w=600&q=80"},
                {"Kissan Fresh Tomato Ketchup (950g)", "Snacks", "135.00", 30, "Made with 100% real ripe juicy tomatoes, tangy and sweet table sauce.", "https://images.unsplash.com/photo-1607349913338-fca6f7fc42d0?auto=format&fit=crop&w=600&q=80"},

                // Bakery
                {"Britannia 100% Whole Wheat Bread (400g)", "Bakery", "50.00", 30, "Freshly baked high-fiber 100% whole wheat sliced brown bread.", "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=600&q=80"},
                {"Sunfeast Dark Fantasy Choco Fills (300g)", "Bakery", "120.00", 40, "Rich crunchy crust cookies with molten velvety choco lava core.", "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=600&q=80"},
                {"Kelloggs Real Almond Corn Flakes (1kg)", "Bakery", "425.00", 25, "Crispy golden corn flakes enriched with sliced crunchy almonds and honey.", "https://images.unsplash.com/photo-1584776296944-ab6fb57b0bdd?auto=format&fit=crop&w=600&q=80"},

                // Personal Care
                {"Dettol Original Germ Protection Soap (4 x 125g)", "Personal Care", "195.00", 35, "Trusted antiseptic germ protection moisturizing skin bath soap.", "https://images.unsplash.com/photo-1600857544200-b2f666a9a2ec?auto=format&fit=crop&w=600&q=80"},
                {"Colgate Total Advanced Health Toothpaste (240g)", "Personal Care", "210.00", 40, "12-hour antibacterial whole mouth protection for teeth and gums.", "https://images.unsplash.com/photo-1559591937-e62fb330bc1f?auto=format&fit=crop&w=600&q=80"},

                // Household
                {"Surf Excel Matic Top Load Detergent (2kg)", "Household", "430.00", 30, "Advanced stain removal formula specially designed for washing machines.", "https://images.unsplash.com/photo-1584813470613-5b1c1cad3d69?auto=format&fit=crop&w=600&q=80"},
                {"Vim Lemon Dishwash Gel (750ml Bottle)", "Household", "155.00", 45, "Concentrated lemon degreasing liquid gel for sparkling clean utensils.", "https://images.unsplash.com/photo-1585670270608-b4be4fbcf05d?auto=format&fit=crop&w=600&q=80"}
        };

        for (Object[] prod : products) {
            String name = (String) prod[0];
            String catName = (String) prod[1];
            BigDecimal price = new BigDecimal((String) prod[2]);
            Integer stock = (Integer) prod[3];
            String desc = (String) prod[4];
            String imageUrl = (String) prod[5];

            Category category = categories.get(catName);
            if (category != null && productRepository.findByNameIgnoreCase(name).isEmpty()) {
                productRepository.save(
                        Product.builder()
                                .name(name)
                                .category(category)
                                .price(price)
                                .stock(stock)
                                .description(desc)
                                .imageUrl(imageUrl)
                                .active(true)
                                .build()
                );
                log.info("Seeded product: {}", name);
            }
        }

        log.info("Grocery products seeding completed successfully.");
    }

    private void seedUserIfNotFound(String email, String name, String rawPassword, Role role, String phone) {
        userRepository.findByEmail(email).ifPresentOrElse(
                existingUser -> {
                    existingUser.setPassword(passwordEncoder.encode(rawPassword));
                    existingUser.setActive(true);
                    existingUser.setName(name);
                    userRepository.save(existingUser);
                    log.info("Updated password for default user: {}", email);
                },
                () -> {
                    userRepository.save(
                            User.builder()
                                    .email(email)
                                    .name(name)
                                    .password(passwordEncoder.encode(rawPassword))
                                    .role(role)
                                    .phone(phone)
                                    .active(true)
                                    .build()
                    );
                    log.info("Seeded default user: {}", email);
                }
        );
    }
}