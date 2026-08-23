package com.dmart.service;

import com.dmart.dto.request.AddToCartRequestDto;
import com.dmart.dto.request.UpdateCartItemRequestDto;
import com.dmart.dto.response.CartResponseDto;
import com.dmart.entity.Cart;
import com.dmart.entity.CartItem;
import com.dmart.entity.Product;
import com.dmart.entity.Role;
import com.dmart.entity.User;
import com.dmart.exception.ConflictException;
import com.dmart.exception.ResourceNotFoundException;
import com.dmart.mapper.CartMapper;
import com.dmart.repository.CartItemRepository;
import com.dmart.repository.CartRepository;
import com.dmart.repository.ProductRepository;
import com.dmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponseDto getMyCart() {
        User customer = getCurrentCustomer();
        Cart cart = getOrCreateCart(customer);
        return getCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponseDto addToCart(AddToCartRequestDto dto) {
        User customer = getCurrentCustomer();
        Cart cart = getOrCreateCart(customer);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + dto.getProductId()));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ConflictException("Product is currently unavailable.");
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        int targetQuantity = dto.getQuantity();

        if (existingItemOpt.isPresent()) {
            targetQuantity += existingItemOpt.get().getQuantity();
        }

        if (targetQuantity > product.getStock()) {
            throw new ConflictException("Requested quantity (" + targetQuantity + ") exceeds available stock (" + product.getStock() + ").");
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(targetQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(dto.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        return getCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponseDto updateCartItem(Long cartItemId, UpdateCartItemRequestDto dto) {
        User customer = getCurrentCustomer();
        Cart cart = getOrCreateCart(customer);

        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        Product product = cartItem.getProduct();
        if (product == null) {
            throw new ResourceNotFoundException("Associated product not found for cart item ID: " + cartItemId);
        }

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ConflictException("Product is currently unavailable. Please remove it from your cart.");
        }

        if (dto.getQuantity() > product.getStock()) {
            throw new ConflictException("Requested quantity (" + dto.getQuantity() + ") exceeds available stock (" + product.getStock() + ").");
        }

        cartItem.setQuantity(dto.getQuantity());
        cartItemRepository.save(cartItem);

        return getCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponseDto removeCartItem(Long cartItemId) {
        User customer = getCurrentCustomer();
        Cart cart = getOrCreateCart(customer);

        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        cartItemRepository.delete(cartItem);

        return getCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponseDto clearCart() {
        User customer = getCurrentCustomer();
        Cart cart = getOrCreateCart(customer);

        cartItemRepository.deleteByCartId(cart.getId());

        return cartMapper.toResponseDto(cart, Collections.emptyList());
    }

    private CartResponseDto getCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        return cartMapper.toResponseDto(cart, items);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
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
            throw new AccessDeniedException("Only customers have access to shopping cart features.");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new AccessDeniedException("Customer account is inactive.");
        }

        return user;
    }
}