package com.app.ecom_application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "201")
    private String name;

    @NotBlank(message = "202")
    private String description;

    @NotNull(message = "203")
    @Positive(message = "204")
    private BigDecimal price;

    @NotNull(message = "205")
    @Min(value = 0, message = "206")
    private Integer stockQuantity;

    private String imageUrl;
}
