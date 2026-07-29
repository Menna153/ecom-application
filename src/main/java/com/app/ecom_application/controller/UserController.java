package com.app.ecom_application.controller;

import com.app.ecom_application.dto.UserRequest;
import com.app.ecom_application.dto.UserResponse;
import com.app.ecom_application.exception.ErrorCode;
import com.app.ecom_application.exception.ErrorResponse;
import com.app.ecom_application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")

public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return new ResponseEntity<>(userService.fetchAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','CUSTOMER')")
    public ResponseEntity<UserResponse> getUser(
            Authentication authentication,
            @PathVariable Long id) {

        return userService.fetchUser(authentication.getName(), id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/customer")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody UserRequest userRequest) {
        ErrorCode error = userService.addCustomer(userRequest);
        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(error.getMessage(), error.getCode()));
        }
        return new ResponseEntity<>("Customer created successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody UserRequest userRequest) {
        ErrorCode error = userService.addAdmin(userRequest);
        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(error.getMessage(), error.getCode()));
        }
        return new ResponseEntity<>("Admin created successfully!", HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','CUSTOMER')")
    public ResponseEntity<?> updateUserInfo(Authentication authentication, @PathVariable Long id, @Valid @RequestBody UserRequest updatedUserRequest) {
        ErrorCode error = userService.updateUser(authentication.getName(), id, updatedUserRequest);
        if (error == null) {
            return new ResponseEntity<>("User information updated successfully!", HttpStatus.OK);
        }
        if (error == ErrorCode.UNAUTHORIZED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(error.getMessage(), error.getCode()));
        }

        if (error == ErrorCode.USER_NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(error.getMessage(), error.getCode()));
        }

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(error.getMessage(), error.getCode()));
    }
}
