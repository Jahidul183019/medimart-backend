package com.medimart.controller;

import com.medimart.model.User;
import com.medimart.repository.UserRepository;
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

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}
