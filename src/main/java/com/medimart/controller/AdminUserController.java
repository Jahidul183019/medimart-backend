package com.medimart.controller;

import com.medimart.model.User;
import com.medimart.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Fetch all users
    @GetMapping
    public List<UserController.UserResponse> getAllUsers() {
        List<User> all = userRepository.findAll();

        all.sort(Comparator.comparing(
                User::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return all.stream()
                .map(UserController.UserResponse::fromEntity)
                .toList();
    }

    // Delete user safely
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        try {
            userRepository.delete(user);
        } catch (DataIntegrityViolationException e) {
            // This happens if user has dependent records (orders, carts, payments)
            throw new RuntimeException(
                    "Cannot delete user. Remove dependent records first (orders, cart, payments).", e
            );
        }
    }

    // Optional: Create new user
    @PostMapping
    public User createUser(@RequestBody User user) {
        // You can add password hashing here if needed
        return userRepository.save(user);
    }

    // Optional: Update existing user
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updated) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setFirstName(updated.getFirstName());
        user.setLastName(updated.getLastName());
        user.setEmail(updated.getEmail());
        user.setPhone(updated.getPhone());
        user.setAddress(updated.getAddress());
        user.setRole(updated.getRole());
        user.setUpdatedAt(System.currentTimeMillis() / 1000);

        return userRepository.save(user);
    }
}
