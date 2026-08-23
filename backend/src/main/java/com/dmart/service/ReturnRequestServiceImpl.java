package com.dmart.service;

import com.dmart.dto.request.CreateReturnRequestDto;
import com.dmart.dto.request.UpdateReturnStatusDto;
import com.dmart.dto.response.ReturnRequestResponseDto;
import com.dmart.entity.Order;
import com.dmart.entity.OrderItem;
import com.dmart.entity.OrderStatus;
import com.dmart.entity.Product;
import com.dmart.entity.ReturnRequest;
import com.dmart.entity.ReturnStatus;
import com.dmart.entity.ReturnType;
import com.dmart.entity.Role;
import com.dmart.entity.User;
import com.dmart.exception.ConflictException;
import com.dmart.exception.ResourceNotFoundException;
import com.dmart.mapper.ReturnRequestMapper;
import com.dmart.repository.OrderItemRepository;
import com.dmart.repository.OrderRepository;
import com.dmart.repository.ProductRepository;
import com.dmart.repository.ReturnRequestRepository;
import com.dmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReturnRequestMapper returnRequestMapper;

    @Override
    @Transactional
    public ReturnRequestResponseDto createRequest(CreateReturnRequestDto dto) {
        User customer = getCurrentCustomer();

        // 1. Verify order belongs to logged-in customer
        Order order = orderRepository.findByIdAndUserId(dto.getOrderId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + dto.getOrderId()));

        // 2. Eligibility: Only COMPLETED (Delivered) orders are eligible
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new ConflictException("Only completed/delivered orders are eligible for return or exchange.");
        }

        // 3. Eligibility: Must be within 7 days
        LocalDateTime deliveryDate = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
        if (deliveryDate.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Return/exchange window of 7 days has expired for this order.");
        }

        // 4. Verify order item exists and belongs to the order
        OrderItem orderItem = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with ID: " + dto.getOrderItemId()));

        if (!orderItem.getOrder().getId().equals(order.getId())) {
            throw new IllegalArgumentException("Order item does not belong to the specified order.");
        }

        // 5. Quantity validation
        if (dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        if (dto.getQuantity() > orderItem.getQuantity()) {
            throw new ConflictException("Requested quantity (" + dto.getQuantity() + ") exceeds purchased quantity (" + orderItem.getQuantity() + ").");
        }

        // 6. Check duplicate active requests
        List<ReturnRequest> activeRequests = returnRequestRepository.findByOrderItemIdAndStatusIn(
                orderItem.getId(),
                List.of(ReturnStatus.REQUESTED, ReturnStatus.PENDING, ReturnStatus.APPROVED)
        );
        if (!activeRequests.isEmpty()) {
            throw new ConflictException("An active return or exchange request already exists for this order item.");
        }

        // 7. Save ReturnRequest (Notice: Stock is untouched at creation time)
        ReturnRequest request = ReturnRequest.builder()
                .order(order)
                .orderItem(orderItem)
                .user(customer)
                .type(dto.getType())
                .quantity(dto.getQuantity())
                .reason(dto.getReason())
                .note(dto.getNote())
                .status(ReturnStatus.REQUESTED)
                .build();

        ReturnRequest saved = returnRequestRepository.save(request);
        return returnRequestMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponseDto> getMyRequests() {
        User customer = getCurrentCustomer();
        List<ReturnRequest> requests = returnRequestRepository.findByUserIdOrderByCreatedAtDesc(customer.getId());
        return requests.stream().map(returnRequestMapper::toResponseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponseDto getMyRequestById(Long id) {
        User customer = getCurrentCustomer();
        ReturnRequest request = returnRequestRepository.findByIdAndUserId(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with ID: " + id));
        return returnRequestMapper.toResponseDto(request);
    }

    @Override
    @Transactional
    public ReturnRequestResponseDto cancelMyRequest(Long id) {
        User customer = getCurrentCustomer();
        ReturnRequest request = returnRequestRepository.findByIdAndUserId(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with ID: " + id));

        if (request.getStatus() == ReturnStatus.COMPLETED) {
            throw new ConflictException("Cannot cancel a completed return or exchange request.");
        }
        if (request.getStatus() == ReturnStatus.REJECTED || request.getStatus() == ReturnStatus.CANCELLED) {
            throw new ConflictException("Request is already in a terminal state: " + request.getStatus());
        }

        request.setStatus(ReturnStatus.CANCELLED);
        ReturnRequest saved = returnRequestRepository.save(request);
        return returnRequestMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponseDto> getAllRequests(ReturnStatus status, ReturnType type) {
        verifyStaffOrAdmin();

        List<ReturnRequest> list;
        if (status != null && type != null) {
            list = returnRequestRepository.findByStatusAndTypeOrderByCreatedAtDesc(status, type);
        } else if (status != null) {
            list = returnRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        } else if (type != null) {
            list = returnRequestRepository.findByTypeOrderByCreatedAtDesc(type);
        } else {
            list = returnRequestRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream().map(returnRequestMapper::toResponseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponseDto getRequestById(Long id) {
        verifyStaffOrAdmin();
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with ID: " + id));
        return returnRequestMapper.toResponseDto(request);
    }

    @Override
    @Transactional
    public ReturnRequestResponseDto updateRequestStatus(Long id, UpdateReturnStatusDto dto) {
        verifyStaffOrAdmin();

        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with ID: " + id));

        ReturnStatus current = request.getStatus();
        ReturnStatus target = dto.getStatus();

        if (current == target) {
            if (dto.getStaffComment() != null) {
                request.setStaffComment(dto.getStaffComment());
                request = returnRequestRepository.save(request);
            }
            return returnRequestMapper.toResponseDto(request);
        }

        if (current == ReturnStatus.COMPLETED || current == ReturnStatus.REJECTED || current == ReturnStatus.CANCELLED) {
            throw new ConflictException("Cannot change status of a " + current + " return/exchange request.");
        }

        // Validate legal transitions
        boolean validTransition = false;
        if (current == ReturnStatus.REQUESTED || current == ReturnStatus.PENDING) {
            validTransition = (target == ReturnStatus.APPROVED || target == ReturnStatus.REJECTED || target == ReturnStatus.CANCELLED);
        } else if (current == ReturnStatus.APPROVED) {
            validTransition = (target == ReturnStatus.COMPLETED || target == ReturnStatus.REJECTED || target == ReturnStatus.CANCELLED);
        }

        if (!validTransition) {
            throw new ConflictException("Invalid status transition from " + current + " to " + target);
        }

        // Inventory safety handling when status transitions to COMPLETED
        if (target == ReturnStatus.COMPLETED) {
            Long productId = request.getOrderItem().getProduct().getId();
            Product product = productRepository.findByIdWithLock(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            if (request.getType() == ReturnType.RETURN) {
                // Return: Restore returned quantity back to stock
                product.setStock(product.getStock() + request.getQuantity());
                productRepository.save(product);
            } else if (request.getType() == ReturnType.EXCHANGE) {
                // Exchange: Customer gets replacement product -> verify and deduct stock
                if (product.getStock() < request.getQuantity()) {
                    throw new ConflictException("Insufficient replacement stock to complete exchange. Available: "
                            + product.getStock() + ", Required: " + request.getQuantity());
                }
                product.setStock(product.getStock() - request.getQuantity());
                productRepository.save(product);
            }
        }

        request.setStatus(target);
        if (dto.getStaffComment() != null) {
            request.setStaffComment(dto.getStaffComment());
        }

        ReturnRequest updated = returnRequestRepository.save(request);
        return returnRequestMapper.toResponseDto(updated);
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
            throw new AccessDeniedException("Only customers have access to customer return/exchange operations.");
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