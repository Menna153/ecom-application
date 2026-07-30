package com.app.ecom_application.repository;

import com.app.ecom_application.model.Order;
import com.app.ecom_application.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdAndStatusAndCreatedAtBetween(
            Long userId,
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    Page<Order> findByUserUsernameAndStatusAndCreatedAtBetween(
            String username,
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    Page<Order> findByUserUsernameAndCreatedAtBetween(
            String username,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    Page<Order> findByStatusAndCreatedAtBetween(
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    Page<Order> findByUserIdAndStatusNotAndCreatedAtBetween(
            Long userId,
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    Page<Order> findByUserUsernameAndStatusNotAndCreatedAtBetween(
            String username,
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    Page<Order> findByCreatedAtBetweenAndStatusNot(
            LocalDateTime from,
            LocalDateTime to,
            OrderStatus status,
            Pageable pageable);
}