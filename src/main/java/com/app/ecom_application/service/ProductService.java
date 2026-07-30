package com.app.ecom_application.service;

import com.app.ecom_application.dto.ProductRequest;
import com.app.ecom_application.dto.ProductResponse;
import com.app.ecom_application.model.Product;
import com.app.ecom_application.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = new Product();
        if (productRepository.existsByNameIgnoreCaseAndActiveTrue(productRequest.getName())) {
            throw new IllegalArgumentException("PRODUCT_NAME_EXISTS");
        }
        makeProductFromRequest(product, productRequest);
        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("INVALID_PRODUCT_ID"));

        if (!product.isActive()) {
            throw new IllegalArgumentException("INVALID_PRODUCT_ID");
        }
        if (!product.getName().equalsIgnoreCase(request.getName())
                && productRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())) {

            throw new IllegalArgumentException("PRODUCT_NAME_EXISTS");
        }
        makeProductFromRequest(product, request);

        return mapToProductResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts(String name, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (name == null || name.isBlank()) {
            return productRepository.findByActiveTrue(pageable)
                    .stream()
                    .map(this::mapToProductResponse)
                    .toList();
        }

        return productRepository
                .findByActiveTrueAndNameContainingIgnoreCase(name, pageable)
                .stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    public ProductResponse getProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("INVALID_PRODUCT_ID"));

        if (!product.isActive()) {
            throw new IllegalArgumentException("INVALID_PRODUCT_ID");
        }

        return mapToProductResponse(product);
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("INVALID_PRODUCT_ID"));

        if (!product.isActive()) {
            throw new IllegalArgumentException("INVALID_PRODUCT_ID");
        }

        product.setActive(false);
        productRepository.save(product);
    }

    private ProductResponse mapToProductResponse(Product savedProduct) {
        ProductResponse response = new ProductResponse();
        response.setId(savedProduct.getId());
        response.setName(savedProduct.getName());
        response.setDescription(savedProduct.getDescription());
        response.setPrice(savedProduct.getPrice());
        response.setStockQuantity(savedProduct.getStockQuantity());
        response.setImageUrl(savedProduct.getImageUrl());
        response.setActive(savedProduct.isActive());
        return response;
    }

    private void makeProductFromRequest(Product product, ProductRequest productRequest) {
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setImageUrl(productRequest.getImageUrl());
    }
}
