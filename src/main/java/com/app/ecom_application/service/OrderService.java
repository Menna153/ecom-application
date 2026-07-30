package com.app.ecom_application.service;

import com.app.ecom_application.dto.OrderItemDTO;
import com.app.ecom_application.dto.OrderResponse;
import com.app.ecom_application.model.*;
import com.app.ecom_application.repository.OrderRepository;
import com.app.ecom_application.repository.ProductRepository;
import com.app.ecom_application.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderResponse createOrder(String userId) {

        List<CartItem> cartItems = cartService.getCartItems(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("CART_EMPTY");
        }

        User user = userRepository.findByUsername(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if(!product.isActive()) {
                throw new IllegalArgumentException("INVALID_PRODUCT_ID");
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("PRODUCT_OUT_OF_STOCK");
            }
        }

        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(
                        null,
                        item.getProduct(),
                        item.getQuantity(),
                        item.getPrice(),
                        order))
                .toList();

        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setStockQuantity(
                    product.getStockQuantity() - item.getQuantity()
            );
            productRepository.save(product);
        }

        cartService.clearCart(userId);

        return mapToOrderResponse(savedOrder);
    }

    public Page<OrderResponse> getAllOrders(
            String userId,
            LocalDateTime from,
            LocalDateTime to,
            OrderStatus status,
            int page,
            int size) {

        User user = userRepository.findByUsername(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        if (from == null) {
            from = LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        if (to == null) {
            to = LocalDateTime.now();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders;

        if (status != null) {
            orders = orderRepository.findByUserIdAndStatusAndCreatedAtBetween(
                    user.getId(),
                    status,
                    from,
                    to,
                    pageable
            );
        } else {
            orders = orderRepository.findByUserIdAndStatusNotAndCreatedAtBetween(
                    user.getId(),
                    OrderStatus.CANCELLED,
                    from,
                    to,
                    pageable
            );
        }

        return orders.map(this::mapToOrderResponse);
    }

    public OrderResponse getOrder(String username, Long orderId) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("INVALID_ORDER_ID"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("INVALID_ORDER_ID");
        }

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access");
        }

        return mapToOrderResponse(order);
    }

    public void deleteOrder(String username, Long orderId) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("INVALID_ORDER_ID"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("INVALID_ORDER_ID");
        }

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public Page<OrderResponse> getAllOrdersForAdmin(
            String username,
            LocalDateTime from,
            LocalDateTime to,
            OrderStatus status,
            int page,
            int size) {

        if (from == null) {
            from = LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        if (to == null) {
            to = LocalDateTime.now();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders;

        if (username != null && status != null) {

            orders = orderRepository.findByUserUsernameAndStatusAndCreatedAtBetween(
                    username,
                    status,
                    from,
                    to,
                    pageable
            );

        } else if (username != null) {

            orders = orderRepository.findByUserUsernameAndStatusNotAndCreatedAtBetween(
                    username,
                    OrderStatus.CANCELLED,
                    from,
                    to,
                    pageable
            );

        } else if (status != null) {

            orders = orderRepository.findByStatusAndCreatedAtBetween(
                    status,
                    from,
                    to,
                    pageable
            );

        } else {

            orders = orderRepository.findByCreatedAtBetweenAndStatusNot(
                    from,
                    to,
                    OrderStatus.CANCELLED,
                    pageable
            );
        }

        return orders.map(this::mapToOrderResponse);
    }

    private OrderResponse mapToOrderResponse(Order savedOrder) {
        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getItems().stream()
                        .map(orderItem -> new OrderItemDTO(
                                orderItem.getId(),
                                orderItem.getProduct().getId(),
                                orderItem.getQuantity(),
                                orderItem.getPrice()
                        ))
                        .toList(),
                savedOrder.getCreatedAt()
        );
    }
}
