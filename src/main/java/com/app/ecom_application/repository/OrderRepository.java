package com.app.ecom_application.repository;

import com.app.ecom_application.model.Order;
import com.app.ecom_application.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUserIdAndId(Long userId, Long id);

    List<Order> findByUserIdAndStatusAndCreatedAtBetween(
            Long userId,
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to);

    List<Order> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime from,
            LocalDateTime to);

    List<Order> findByUserUsernameAndStatusAndCreatedAtBetween(String username, OrderStatus status, LocalDateTime from, LocalDateTime to);

    List<Order> findByUserUsernameAndCreatedAtBetween(String username, LocalDateTime from, LocalDateTime to);

    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime from, LocalDateTime to);

    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}