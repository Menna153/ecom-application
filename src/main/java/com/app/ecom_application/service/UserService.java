package com.app.ecom_application.service;

import com.app.ecom_application.dto.AddressDTO;
import com.app.ecom_application.dto.UserRequest;
import com.app.ecom_application.dto.UserResponse;
import com.app.ecom_application.exception.ErrorCode;
import com.app.ecom_application.model.Address;
import com.app.ecom_application.model.User;
import com.app.ecom_application.model.UserRole;
import com.app.ecom_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

    public ErrorCode addCustomer(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ErrorCode.USERNAME_ALREADY_EXISTS;
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ErrorCode.EMAIL_ALREADY_EXISTS;
        }
        createUser(request, UserRole.CUSTOMER);
        return null;
    }

    public ErrorCode addAdmin(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ErrorCode.USERNAME_ALREADY_EXISTS;
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ErrorCode.EMAIL_ALREADY_EXISTS;
        }
        createUser(request, UserRole.ADMIN);
        return null;
    }

    private void createUser(UserRequest request, UserRole role) {
        User user = new User();
        makeUserFromRequest(user, request);
        user.setRole(role);
        userRepository.save(user);
    }

    public Optional<UserResponse> fetchUser(String username, Long id) {

        User loggedInUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        if (loggedInUser.getRole() == UserRole.CUSTOMER
                && !loggedInUser.getId().equals(id)) {
            throw new AccessDeniedException("You do not have access");
        }

        return userRepository.findById(id)
                .map(this::mapToUserResponse);
    }

    public ErrorCode updateUser(String loggedInUsername, Long id, UserRequest request) {
        Optional<User> loggedUser =
                userRepository.findByUsername(loggedInUsername);

        Optional<User> targetUser =
                userRepository.findById(id);

        if (loggedUser.isEmpty() || targetUser.isEmpty()) {
            return ErrorCode.USER_NOT_FOUND;
        }
        User current = loggedUser.get();
        User target = targetUser.get();

        if (current.getRole() != UserRole.ADMIN &&
                current.getRole() != UserRole.SUPER_ADMIN &&
                !current.getId().equals(target.getId())) {
            return ErrorCode.UNAUTHORIZED;
        }
        if (!target.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            return ErrorCode.USERNAME_ALREADY_EXISTS;
        }

        if (!target.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            return ErrorCode.EMAIL_ALREADY_EXISTS;
        }

        makeUserFromRequest(target, request);
        userRepository.save(target);
        return null;
    }

    private void makeUserFromRequest(User user, UserRequest userRequest) {
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        Address address = new Address();
        address.setStreet(userRequest.getAddress().getStreet());
        address.setApartment(userRequest.getAddress().getApartment());
        address.setCity(userRequest.getAddress().getCity());
        address.setCountry(userRequest.getAddress().getCountry());
        user.setAddress(address);

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
