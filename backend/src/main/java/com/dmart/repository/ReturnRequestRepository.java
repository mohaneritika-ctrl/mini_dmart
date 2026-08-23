package com.dmart.repository;

import com.dmart.entity.ReturnRequest;
import com.dmart.entity.ReturnStatus;
import com.dmart.entity.ReturnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByUserId(Long userId);

    List<ReturnRequest> findByOrderId(Long orderId);

    List<ReturnRequest> findByStatus(ReturnStatus status);

    List<ReturnRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ReturnRequest> findByIdAndUserId(Long id, Long userId);

    List<ReturnRequest> findAllByOrderByCreatedAtDesc();

    List<ReturnRequest> findByStatusOrderByCreatedAtDesc(ReturnStatus status);

    List<ReturnRequest> findByTypeOrderByCreatedAtDesc(ReturnType type);

    List<ReturnRequest> findByStatusAndTypeOrderByCreatedAtDesc(ReturnStatus status, ReturnType type);

    List<ReturnRequest> findByOrderItemIdAndStatusIn(Long orderItemId, List<ReturnStatus> statuses);
}