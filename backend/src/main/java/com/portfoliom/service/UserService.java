package com.portfoliom.service;

import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import com.portfoliom.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()));
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    public User getCurrentUser(Authentication authentication) {
        return getByUsername(authentication.getName());
    }

    public Long getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getId();
    }

    public List<User> listCustomers() {
        return userRepository.findByRole(Role.OWNER);
    }

    public User createCustomer(String username, String rawPassword, String name, String email, User managedBy) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }
        User user = new User(username, passwordEncoder.encode(rawPassword), Role.OWNER);
        user.setName(name);
        user.setEmail(email);
        user.setManagedBy(managedBy);
        return userRepository.save(user);
    }

    public User getCustomerById(Long id) {
        return userRepository.findById(id)
                .filter(u -> u.getRole() == Role.OWNER)
                .orElseThrow(() -> new IllegalStateException("Customer not found with id: " + id));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
