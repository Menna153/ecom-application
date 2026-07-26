package com.app.ecom_application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressDTO {
    @NotBlank(message = "Street name is required")
    private String street;

    @NotBlank(message = "Apartment number is required")
    private String apartment;

    @NotBlank(message = "City field is required")
    private String city;

    @NotBlank(message = "Country field is required")
    private String country;
}
