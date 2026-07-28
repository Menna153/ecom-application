package com.app.ecom_application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "103")
    private String firstName;

    @NotBlank(message = "104")
    private String lastName;

    @NotBlank(message = "105")
    @Email(message = "106")
    private String email;

    @NotBlank(message = "107")
    private String phone;

    @NotNull(message = "108")
    @Valid
    private AddressDTO address;

    @NotBlank(message = "101")
    private String username;

    @NotBlank(message = "102")
    private String password;
}
