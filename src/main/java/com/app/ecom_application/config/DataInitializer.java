package com.app.ecom_application.config;

import com.app.ecom_application.model.Address;
import com.app.ecom_application.model.Product;
import com.app.ecom_application.model.User;
import com.app.ecom_application.model.UserRole;
import com.app.ecom_application.repository.ProductRepository;
import com.app.ecom_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Super Admin
        if (!userRepository.existsByUsername("superadmin")) {

            Address address = new Address();
            address.setStreet("N/A");
            address.setApartment("N/A");
            address.setCity("N/A");
            address.setCountry("N/A");

            User superAdmin = new User();
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setUsername("superadmin");
            superAdmin.setEmail("superadmin@test.com");
            superAdmin.setPhone("0000000000");
            superAdmin.setPassword(passwordEncoder.encode("admin123"));
            superAdmin.setRole(UserRole.SUPER_ADMIN);
            superAdmin.setAddress(address);

            userRepository.save(superAdmin);
        }

        // Admin
        if (!userRepository.existsByUsername("admin")) {

            Address address = new Address();
            address.setStreet("Admin Street");
            address.setApartment("10");
            address.setCity("Cairo");
            address.setCountry("Egypt");

            User admin = new User();
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setUsername("admin");
            admin.setEmail("admin@test.com");
            admin.setPhone("01111111111");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setAddress(address);

            userRepository.save(admin);
        }

        // Customer
        if (!userRepository.existsByUsername("user1")) {

            Address address = new Address();
            address.setStreet("Customer Street");
            address.setApartment("5");
            address.setCity("Giza");
            address.setCountry("Egypt");

            User customer = new User();
            customer.setFirstName("Test");
            customer.setLastName("User");
            customer.setUsername("user1");
            customer.setEmail("user1@test.com");
            customer.setPhone("01234567890");
            customer.setPassword(passwordEncoder.encode("user123"));
            customer.setRole(UserRole.CUSTOMER);
            customer.setAddress(address);

            userRepository.save(customer);
        }

        // Products
        if (productRepository.count() == 0) {

            Product p1 = new Product();
            p1.setName("iPhone 15");
            p1.setDescription("Apple smartphone");
            p1.setPrice(BigDecimal.valueOf(1000));
            p1.setStockQuantity(20);
            p1.setImageUrl("iphone.jpg");
            p1.setActive(true);

            Product p2 = new Product();
            p2.setName("Samsung S25");
            p2.setDescription("Samsung smartphone");
            p2.setPrice(BigDecimal.valueOf(900));
            p2.setStockQuantity(15);
            p2.setImageUrl("samsung.jpg");
            p2.setActive(true);

            productRepository.save(p1);
            productRepository.save(p2);
        }
    }
}