package com.app.ecom_application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "301")
    private Long productId;

    @NotNull(message = "302")
    @Positive(message = "303")
    private Integer quantity;
}
