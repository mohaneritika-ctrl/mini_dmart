package com.dmart.repository;

import com.dmart.entity.Order;
import com.dmart.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByOrderStatusOrderByCreatedAtDesc(OrderStatus orderStatus);

    long countByUserId(Long userId);
}