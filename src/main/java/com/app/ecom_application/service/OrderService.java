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
        List<CartItem> cartItems = cartService.getCart(userId);
        if(cartItems.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
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
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);
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

    public Optional<List<OrderResponse>> getAllOrders(String userId) {
        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        if(userOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(orderRepository.findByUserId(Long.valueOf(userId))
                .stream().filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(this::mapToOrderResponse).toList());
    }

    public Optional<OrderResponse> getOrder(String userId, Long orderId) {
        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        if(userOpt.isEmpty()) {
            return Optional.empty();
        }
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if(orderOpt.isEmpty() || orderOpt.get().getStatus().equals(OrderStatus.CANCELLED)) {
            return Optional.empty();
        }
        return orderRepository.findByUserIdAndId(Long.valueOf(userId), orderId)
                .map(this::mapToOrderResponse);
    }

    public boolean deleteOrder(String userId, Long orderId) {
        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        if(userOpt.isEmpty()) {
            return false;
        }
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if(orderOpt.isEmpty() || orderOpt.get().getStatus().equals(OrderStatus.CANCELLED)) {
            return false;
        }
        return orderRepository.findById(orderId)
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
}
