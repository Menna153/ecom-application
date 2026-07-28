package com.app.ecom_application.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "101")
    private String username;

    @NotBlank(message = "102")
    private String password;
}
