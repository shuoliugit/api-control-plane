package com.example.apicontrolplane.config;

import com.example.apicontrolplane.user.Role;
import com.example.apicontrolplane.user.UserAccount;
import com.example.apicontrolplane.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
  @Bean
  CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder,
      @Value("${ADMIN_EMAIL:admin@controlplane.local}") String email,
      @Value("${ADMIN_PASSWORD:AdminPassword123!}") String password) {
    return args -> {
      if (!users.existsByEmail(email.toLowerCase())) {
        users.save(new UserAccount(email, encoder.encode(password), "Platform Admin", Role.ADMIN));
      }
    };
  }
}
