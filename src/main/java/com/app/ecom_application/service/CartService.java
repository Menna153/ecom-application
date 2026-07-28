package com.app.ecom_application.service;

import com.app.ecom_application.dto.CartItemRequest;
import com.app.ecom_application.dto.CartItemResponse;
import com.app.ecom_application.model.CartItem;
import com.app.ecom_application.model.Product;
import com.app.ecom_application.model.User;
import com.app.ecom_application.repository.CartItemRepository;
import com.app.ecom_application.repository.ProductRepository;
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
public class CartService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public boolean addToCart(String userId, CartItemRequest request) {
        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if(productOpt.isEmpty()) {
            return false;
        }
        Product product = productOpt.get();
        if(!product.isActive()) {
            return false;
        }
        Optional<User> userOpt = userRepository.findByUsername(userId);
        if(userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        CartItem existingCartItem = cartItemRepository.findByUserAndProduct(user, product);
        int totalQuantity = request.getQuantity();
        if (existingCartItem != null) {
            totalQuantity += existingCartItem.getQuantity();
        }
        if (product.getStockQuantity() < totalQuantity) {
            return false;
        }
        if (existingCartItem != null) {
            existingCartItem.setQuantity(totalQuantity);
            existingCartItem.setPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(totalQuantity))
            );
            cartItemRepository.save(existingCartItem);
        }
        else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(totalQuantity);
            cartItem.setPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(totalQuantity))
            );
            cartItemRepository.save(cartItem);
        }
        return true;
    }

    public boolean deleteFromCart(String userId, Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<User> userOpt = userRepository.findByUsername(userId);
        if(productOpt.isPresent() && userOpt.isPresent()) {
            cartItemRepository.deleteByUserAndProduct(userOpt.get(), productOpt.get());
            return true;
        }
        return false;
    }

    public List<CartItemResponse> getCart(String userId) {
        return userRepository.findByUsername(userId)
                .map(cartItemRepository::findByUser)
                .orElse(List.of())
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();
    }

    public List<CartItem> getCartItems(String userId) {
        return userRepository.findByUsername(userId)
                .map(cartItemRepository::findByUser)
                .orElse(List.of());
    }

    public void clearCart(String userId) {
        userRepository.findByUsername(userId)
                .ifPresent(cartItemRepository::deleteByUser);
    }

    public CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setProductId(cartItem.getProduct().getId());
        cartItemResponse.setProductName(cartItem.getProduct().getName());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        cartItemResponse.setPrice(cartItem.getPrice());
        return cartItemResponse;
    }
}
