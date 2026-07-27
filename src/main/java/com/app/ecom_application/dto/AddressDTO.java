package com.app.ecom_application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressDTO {
    @NotBlank(message = "110")
    private String street;

    @NotBlank(message = "111")
    private String apartment;

    @NotBlank(message = "112")
    private String city;

    @NotBlank(message = "113")
    private String country;
}
