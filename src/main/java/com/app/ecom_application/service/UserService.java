package com.app.ecom_application.service;

import com.app.ecom_application.dto.AddressDTO;
import com.app.ecom_application.dto.UserRequest;
import com.app.ecom_application.dto.UserResponse;
import com.app.ecom_application.model.Address;
import com.app.ecom_application.model.User;
import com.app.ecom_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> fetchAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public void addUser(UserRequest userRequest) {
        User user = new User();
        makeUserFromRequest(user, userRequest);
        userRepository.save(user);
    }

    public Optional<UserResponse> fetchUser(Long id) {
        return userRepository.findById(id).map(this::mapToUserResponse);
    }

    public boolean updateUser(Long id, UserRequest updatedUserRequest) {
        return userRepository.findById(id).map(existingUser -> {
            makeUserFromRequest(existingUser, updatedUserRequest);
            userRepository.save(existingUser);
            return true;
        }).orElse(false);
    }

    private void makeUserFromRequest(User user, UserRequest userRequest) {
        if(userRequest.getFirstName() != null) {
            user.setFirstName(userRequest.getFirstName());
        }
        if(userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
        }
        if(userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
        }
        if(userRequest.getPhone() != null) {
            user.setPhone(userRequest.getPhone());
        }
        if(userRequest.getUsername() != null) {
            user.setUsername(userRequest.getUsername());
        }
        if(userRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        if(userRequest.getUserRole() != null) {
            user.setRole(userRequest.getUserRole());
        }
        if(userRequest.getAddress() != null) {
            Address address = new Address();
            address.setStreet(userRequest.getAddress().getStreet());
            address.setApartment(userRequest.getAddress().getApartment());
            address.setCity(userRequest.getAddress().getCity());
            address.setCountry(userRequest.getAddress().getCountry());
            user.setAddress(address);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(String.valueOf(user.getId()));
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());

        if(user.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setStreet(user.getAddress().getStreet());
            addressDTO.setApartment(user.getAddress().getApartment());
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setCountry(user.getAddress().getCountry());
            response.setAddress(addressDTO);
        }
        return response;
    }
}
