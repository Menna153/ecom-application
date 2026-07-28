package com.app.ecom_application.controller;

import com.app.ecom_application.dto.OrderResponse;
import com.app.ecom_application.model.OrderStatus;
import com.app.ecom_application.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication) {
        return orderService.createOrder(authentication.getName())
                .map(orderResponse -> new ResponseEntity<>(orderResponse, HttpStatus.CREATED)
                ).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(Authentication authentication, @RequestParam(required = false) LocalDateTime from, @RequestParam(required = false) LocalDateTime to, @RequestParam(required = false) OrderStatus status) {

        return orderService
                .getAllOrders(authentication.getName(), from, to, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> getOrder(Authentication authentication, @PathVariable Long orderId) {
        return orderService.getOrder(authentication.getName(), orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteOrder(Authentication authentication, @PathVariable Long orderId) {

        boolean deleted = orderService.deleteOrder(authentication.getName(), orderId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrdersForAdmin(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) OrderStatus status) {

        return ResponseEntity.ok(
                orderService.getAllOrdersForAdmin(username, from, to, status)
        );
    }
}
