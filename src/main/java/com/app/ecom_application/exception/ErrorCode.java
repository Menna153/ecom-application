package com.app.ecom_application.exception;

public enum ErrorCode {
    USERNAME_REQUIRED(101, "Username is required"),
    PASSWORD_REQUIRED(102, "Password is required"),
    FIRST_NAME_REQUIRED(103, "First name is required"),
    LAST_NAME_REQUIRED(104, "Last name is required"),
    EMAIL_REQUIRED(105, "Email is required"),
    INVALID_EMAIL(106, "Invalid email"),
    PHONE_REQUIRED(107, "Phone number is required"),
    ADDRESS_REQUIRED(108, "Address is required"),
    ROLE_REQUIRED(109, "Role is required"),
    STREET_REQUIRED(110, "Street name is required"),
    APARTMENT_REQUIRED(111, "Apartment number is required"),
    CITY_REQUIRED(112, "City field is required"),
    COUNTRY_REQUIRED(113, "Country field is required"),
    PRODUCT_NAME_REQUIRED(201, "Product name is required"),
    PRODUCT_DESCRIPTION_REQUIRED(202, "Description is required"),
    PRICE_REQUIRED(203, "Price is required"),
    PRICE_MUST_BE_POSITIVE(204, "Price must be greater than 0"),
    STOCK_REQUIRED(205, "Stock quantity is required"),
    STOCK_NEGATIVE(206, "Stock cannot be negative"),
    PRODUCT_ID_REQUIRED(301, "Product Id is required"),
    QUANTITY_REQUIRED(302, "Quantity is required"),
    QUANTITY_POSITIVE(303, "Quantity must be greater than 0"),
    REFRESH_TOKEN_REQUIRED(401, "Refresh token is required"),

    VALIDATION_FAILED(1003, "Validation failed"),

    INTERNAL_SERVER_ERROR(5000, "Internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorCode fromCode(int code) {
        for (ErrorCode value : values()) {
            if (value.code == code)
                return value;
        }
        return INTERNAL_SERVER_ERROR;
    }
}
