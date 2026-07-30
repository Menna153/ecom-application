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

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public void addToCart(String userId, CartItemRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new IllegalArgumentException("INVALID_PRODUCT_ID"));

        if (!product.isActive()) {
            throw new IllegalArgumentException("INVALID_PRODUCT_ID");
        }

        User user = userRepository.findByUsername(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        CartItem existingCartItem =
                cartItemRepository.findByUserAndProduct(user, product);

        int totalQuantity = request.getQuantity();

        if (existingCartItem != null) {
            totalQuantity += existingCartItem.getQuantity();
        }

        if (product.getStockQuantity() < totalQuantity) {
            throw new IllegalArgumentException("PRODUCT_OUT_OF_STOCK");
        }

        if (existingCartItem != null) {
            existingCartItem.setQuantity(totalQuantity);
            existingCartItem.setPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(totalQuantity))
            );
            cartItemRepository.save(existingCartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(totalQuantity);
            cartItem.setPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(totalQuantity))
            );
            cartItemRepository.save(cartItem);
        }
    }

    public void deleteFromCart(String userId, Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("INVALID_PRODUCT_ID"));

        if (!product.isActive()) {
            throw new IllegalArgumentException("INVALID_PRODUCT_ID");
        }

        User user = userRepository.findByUsername(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product);

        if (cartItem == null) {
            throw new IllegalArgumentException("CART_ITEM_NOT_FOUND");
        }

        cartItemRepository.delete(cartItem);
    }

    public List<CartItemResponse> getCart(String userId) {

        User user = userRepository.findByUsername(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        return cartItemRepository.findByUser(user)
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();
    }

    public List<CartItem> getCartItems(String userId) {

        User user = userRepository.findByUsername(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        return cartItemRepository.findByUser(user);
    }

    public void clearCart(String userId) {

        User user = userRepository.findByUsername(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        cartItemRepository.deleteByUser(user);
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setProductId(cartItem.getProduct().getId());
        cartItemResponse.setProductName(cartItem.getProduct().getName());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        cartItemResponse.setPrice(cartItem.getPrice());
        return cartItemResponse;
    }
}
