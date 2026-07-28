package com.app.ecom_application.service;

import com.app.ecom_application.dto.OrderItemDTO;
import com.app.ecom_application.dto.OrderResponse;
import com.app.ecom_application.model.*;
import com.app.ecom_application.repository.OrderRepository;
import com.app.ecom_application.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public Optional<OrderResponse> createOrder(String userId) {
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if(cartItems.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> userOpt = userRepository.findByUsername(userId);
        if(userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                return Optional.empty();
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
                        order
                )).toList();
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);
        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity() - item.getQuantity()
            );
        }
        cartService.clearCart(userId);
        return Optional.of(mapToOrderResponse(savedOrder));
    }

    public Optional<List<OrderResponse>> getAllOrders(String userId, LocalDateTime from, LocalDateTime to, OrderStatus status) {
        Optional<User> userOpt = userRepository.findByUsername(userId);
        if(userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        List<Order> orders;

        if (from == null) {
            from = LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        if (to == null) {
            to = LocalDateTime.now();
        }

        if (status != null) {
            orders = orderRepository.findByUserIdAndStatusAndCreatedAtBetween(
                    user.getId(),
                    status,
                    from,
                    to
            );
        } else {
            orders = orderRepository.findByUserIdAndCreatedAtBetween(
                    user.getId(),
                    from,
                    to
            );
        }

        return Optional.of(
                orders.stream()
                        .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                        .map(this::mapToOrderResponse)
                        .toList()
        );
    }

    public Optional<OrderResponse> getOrder(String userId, Long orderId) {
        Optional<User> userOpt = userRepository.findByUsername(userId);
        if(userOpt.isEmpty()) {
            return Optional.empty();
        }
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if(orderOpt.isEmpty() || orderOpt.get().getStatus().equals(OrderStatus.CANCELLED)) {
            return Optional.empty();
        }
        return orderRepository.findByUserIdAndId(userOpt.get().getId(), orderId)
                .map(this::mapToOrderResponse);
    }

    public boolean deleteOrder(String userId, Long orderId) {
        Optional<User> userOpt = userRepository.findByUsername(userId);
        if(userOpt.isEmpty()) {
            return false;
        }
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if(orderOpt.isEmpty() || orderOpt.get().getStatus().equals(OrderStatus.CANCELLED)) {
            return false;
        }
        return orderRepository.findByUserIdAndId(userOpt.get().getId(), orderId)
                .map(order -> {
                    for (OrderItem item : order.getItems()) {
                        Product product = item.getProduct();

                        product.setStockQuantity(
                                product.getStockQuantity() + item.getQuantity()
                        );
                    }
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    return true;
                })
                .orElse(false);
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
                        )).toList()
        );
    }

    public List<OrderResponse> getAllOrdersForAdmin(
            String username,
            LocalDateTime from,
            LocalDateTime to,
            OrderStatus status) {

        if (from == null) {
            from = LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        if (to == null) {
            to = LocalDateTime.now();
        }

        List<Order> orders;

        if (username != null && status != null) {
            orders = orderRepository.findByUserUsernameAndStatusAndCreatedAtBetween(
                    username, status, from, to);
        } else if (username != null) {
            orders = orderRepository.findByUserUsernameAndCreatedAtBetween(
                    username, from, to);
        } else if (status != null) {
            orders = orderRepository.findByStatusAndCreatedAtBetween(
                    status, from, to);
        } else {
            orders = orderRepository.findByCreatedAtBetween(from, to);
        }

        return orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(this::mapToOrderResponse)
                .toList();
    }
}
