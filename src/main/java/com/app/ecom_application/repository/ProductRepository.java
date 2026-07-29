package com.app.ecom_application.repository;

import com.app.ecom_application.model.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    boolean existsByNameIgnoreCase(String name);
}
