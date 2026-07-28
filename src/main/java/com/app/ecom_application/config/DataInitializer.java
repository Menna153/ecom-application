package com.app.ecom_application.config;

import com.app.ecom_application.model.Address;
import com.app.ecom_application.model.User;
import com.app.ecom_application.model.UserRole;
import com.app.ecom_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByUsername("superadmin")) {

            Address address = new Address();
            address.setStreet("N/A");
            address.setApartment("N/A");
            address.setCity("N/A");
            address.setCountry("N/A");

            User user = new User();
            user.setFirstName("Super");
            user.setLastName("Admin");
            user.setUsername("superadmin");
            user.setEmail("superadmin@test.com");
            user.setPhone("0000000000");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setRole(UserRole.SUPER_ADMIN);
            user.setAddress(address);

            userRepository.save(user);
        }
    }
}
