package com.app.ecom_application.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String street;
    private String apartment;
    private String city;
    private String country;
}
