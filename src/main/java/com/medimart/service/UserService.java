package com.medimart.service;

import com.medimart.model.User;
import com.medimart.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public UserService(UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Register new user (no hashing yet – we can add later)
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        user.setUpdatedAt(Instant.now().getEpochSecond());
        User saved = userRepository.save(user);
        broadcastUsers();
        return saved;
    }

    // Simple login check (plain password compare)
    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Update profile info (no password)
    public User updateProfile(Long id, User updated) {
        User saved = userRepository.findById(id).map(existing -> {
            existing.setFirstName(updated.getFirstName());
            existing.setLastName(updated.getLastName());
            existing.setPhone(updated.getPhone());
            existing.setEmail(updated.getEmail());
            existing.setAddress(updated.getAddress());
            existing.setAvatarPath(updated.getAvatarPath());
            existing.setUpdatedAt(Instant.now().getEpochSecond());
            return userRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));

        broadcastUsers();
        return saved;
    }

    // Change password
    public void changePassword(Long id, String newPassword) {
        userRepository.findById(id).ifPresent(u -> {
            u.setPassword(newPassword);
            u.setUpdatedAt(Instant.now().getEpochSecond());
            userRepository.save(u);
            broadcastUsers();
        });
    }

    /**
     * Push full user list to all WebSocket subscribers.
     * Frontend subscribes to /topic/users (Admin Users tab).
     */
    private void broadcastUsers() {
        List<User> all = userRepository.findAll();
        messagingTemplate.convertAndSend("/topic/users", all);
    }
}
