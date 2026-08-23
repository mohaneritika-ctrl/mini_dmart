package com.dmart.service;

import com.dmart.dto.request.CheckoutRequestDto;
import com.dmart.dto.request.UpdateOrderStatusRequestDto;
import com.dmart.dto.response.OrderResponseDto;
import com.dmart.entity.Cart;
import com.dmart.entity.CartItem;
import com.dmart.entity.Order;
import com.dmart.entity.OrderItem;
import com.dmart.entity.OrderStatus;
import com.dmart.entity.OrderType;
import com.dmart.entity.Product;
import com.dmart.entity.Role;
import com.dmart.entity.User;
import com.dmart.exception.ConflictException;
import com.dmart.exception.ResourceNotFoundException;
import com.dmart.mapper.OrderMapper;
import com.dmart.repository.CartItemRepository;
import com.dmart.repository.CartRepository;
import com.dmart.repository.OrderItemRepository;
import com.dmart.repository.OrderRepository;
import com.dmart.repository.ProductRepository;
import com.dmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PLACED, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED),
            OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
            OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDto checkout(CheckoutRequestDto dto) {
        User customer = getCurrentCustomer();
        Cart cart = cartRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cannot checkout an empty cart."));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an empty cart.");
        }

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        List<OrderItem> orderItemsToSave = new ArrayList<>();

        // 1. Validate all items, lock products, verify active & stock, compute total, reduce stock
        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProduct().getId();
            Product product = productRepository.findByIdWithLock(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new ConflictException("Product is no longer available: " + product.getName());
            }

            if (cartItem.getQuantity() > product.getStock()) {
                throw new ConflictException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStock() + ", Requested: " + cartItem.getQuantity());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            calculatedTotal = calculatedTotal.add(subtotal);

            // Reduce stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 2. Build and save Order
        OrderType orderType = (dto != null && dto.getOrderType() != null) ? dto.getOrderType() : OrderType.PICKUP;
        Order order = Order.builder()
                .user(customer)
                .totalAmount(calculatedTotal.setScale(2, RoundingMode.HALF_UP))
                .orderType(orderType)
                .orderStatus(OrderStatus.CONFIRMED)
                .pickupDate(dto != null ? dto.getPickupDate() : null)
                .pickupTimeSlot(dto != null ? dto.getPickupTimeSlot() : null)
                .deliveryAddress(dto != null ? dto.getDeliveryAddress() : null)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 3. Create OrderItems with price snapshot
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice()) // price snapshot
                    .build();

            OrderItem savedItem = orderItemRepository.save(orderItem);
            orderItemsToSave.add(savedItem);
        }

        // 4. Clear customer's cart
        cartItemRepository.deleteByCartId(cart.getId());

        return orderMapper.toResponseDto(savedOrder, orderItemsToSave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyOrders() {
        User customer = getCurrentCustomer();
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(customer.getId());

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return orderMapper.toResponseDto(order, items);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getMyOrderById(Long orderId) {
        User customer = getCurrentCustomer();
        Order order = orderRepository.findByIdAndUserId(orderId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return orderMapper.toResponseDto(order, items);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelMyOrder(Long orderId) {
        User customer = getCurrentCustomer();
        Order order = orderRepository.findByIdAndUserId(orderId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("Order is already cancelled.");
        }

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new ConflictException("Order cannot be cancelled because it is already completed.");
        }

        if (order.getOrderStatus() != OrderStatus.PLACED && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new ConflictException("Order cannot be cancelled in its current status: " + order.getOrderStatus());
        }

        // Restore stock for all line items
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProduct().getId()));

            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toResponseDto(updatedOrder, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders(OrderStatus status) {
        verifyStaffOrAdmin();

        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByOrderStatusOrderByCreatedAtDesc(status);
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        }

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return orderMapper.toResponseDto(order, items);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByIdForStaff(Long orderId) {
        verifyStaffOrAdmin();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return orderMapper.toResponseDto(order, items);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, UpdateOrderStatusRequestDto dto) {
        verifyStaffOrAdmin();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        OrderStatus currentStatus = order.getOrderStatus();
        OrderStatus newStatus = dto.getStatus();

        if (currentStatus == newStatus) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            return orderMapper.toResponseDto(order, items);
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new ConflictException("Cannot change status of an already cancelled order.");
        }

        if (currentStatus == OrderStatus.COMPLETED) {
            throw new ConflictException("Cannot change status of a completed order.");
        }

        Set<OrderStatus> allowedNextStatuses = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedNextStatuses.contains(newStatus)) {
            throw new ConflictException("Invalid order status transition from " + currentStatus + " to " + newStatus);
        }

        // If transitioning to CANCELLED, restore product stock
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : items) {
                Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProduct().getId()));

                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toResponseDto(updatedOrder, items);
    }

    private User getCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadCredentialsException("User is not authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getRole() != Role.CUSTOMER) {
            throw new AccessDeniedException("Only customers have access to customer order endpoints.");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new AccessDeniedException("Customer account is inactive.");
        }

        return user;
    }

    private void verifyStaffOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadCredentialsException("User is not authenticated");
        }

        boolean isStaffOrAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isStaffOrAdmin) {
            throw new AccessDeniedException("Access denied: Staff or Admin role required.");
        }
    }
}