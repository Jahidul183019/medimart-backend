package com.medimart.controller;

import com.medimart.model.User;
import com.medimart.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==========================
    // 0) SIMPLE SIGNUP (legacy)
    //     POST /api/users/signup
    // ==========================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SimpleSignupRequest req) {
        try {
            User user = new User();
            user.setFirstName(req.name());
            user.setLastName(null);
            user.setPhone(null);
            user.setEmail(req.email());
            user.setPassword(req.password());
            user.setAddress(null);
            user.setAvatarPath(null);
            user.setUpdatedAt(Instant.now().getEpochSecond());

            User saved = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserResponse.fromEntity(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // ==========================
    // 1) REGISTER (full form – frontend)
    //     POST /api/users/register
    // ==========================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User user = new User(
                    req.firstName(),
                    req.lastName(),
                    req.phone(),
                    req.email(),
                    req.password()
            );
            user.setAddress(req.address());
            user.setAvatarPath(null);
            user.setUpdatedAt(Instant.now().getEpochSecond());

            User saved = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserResponse.fromEntity(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // ==========================
    // 2) LOGIN (email + password)
    // ==========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> opt = userService.login(req.email(), req.password());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
        return ResponseEntity.ok(UserResponse.fromEntity(opt.get()));
    }

    // ==========================
    // 3) GET ALL USERS
    // ==========================
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        List<User> users = userService.findAll();
        List<UserResponse> resp = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(resp);
    }

    // ==========================
    // 4) GET PROFILE BY ID
    //     GET /api/users/{id}
    // ==========================
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return userService.findById(id)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(UserResponse.fromEntity(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id " + id));
    }

    // ==========================
    // 5) UPDATE PROFILE (no password)
    //     PUT /api/users/{id}
    // ==========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest req
    ) {
        User updated = new User();
        updated.setFirstName(req.firstName());
        updated.setLastName(req.lastName());
        updated.setPhone(req.phone());
        updated.setEmail(req.email());
        updated.setAddress(req.address());
        updated.setAvatarPath(req.avatarPath());
        updated.setUpdatedAt(Instant.now().getEpochSecond());

        try {
            User saved = userService.updateProfile(id, updated);
            return ResponseEntity.ok(UserResponse.fromEntity(saved));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ==========================
    // 6) CHANGE PASSWORD
    //     PUT /api/users/{id}/password
    // ==========================
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest req
    ) {
        Optional<User> opt = userService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with id " + id);
        }

        User user = opt.get();
        if (!user.getPassword().equals(req.currentPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Current password is incorrect");
        }

        userService.changePassword(id, req.newPassword());
        User updated = userService.findById(id).orElse(user);
        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }

    // ==========================
    // 7) UPLOAD AVATAR
    //     POST /api/users/{id}/avatar  (multipart, field: avatar)
    // ==========================
    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("avatar") MultipartFile avatarFile
    ) {
        if (avatarFile.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }

        Optional<User> opt = userService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with id " + id);
        }

        try {
            Path uploadDir = Paths.get("uploads", "avatars");
            Files.createDirectories(uploadDir);

            String originalName = StringUtils.cleanPath(avatarFile.getOriginalFilename());
            String ext = "";
            int dotIdx = originalName.lastIndexOf('.');
            if (dotIdx != -1) {
                ext = originalName.substring(dotIdx);
            }

            String filename = "user-" + id + "-" + System.currentTimeMillis() + ext;
            Path dest = uploadDir.resolve(filename);

            Files.copy(avatarFile.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "uploads/avatars/" + filename;

            User existing = opt.get();
            existing.setAvatarPath(relativePath);
            existing.setUpdatedAt(Instant.now().getEpochSecond());

            // reuse the existing updateProfile logic
            User saved = userService.updateProfile(id, existing);
            return ResponseEntity.ok(UserResponse.fromEntity(saved));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not store file: " + e.getMessage());
        }
    }

    // =======================================================
    // DTOs
    // =======================================================

    public static record SimpleSignupRequest(
            String name,
            String email,
            String password,
            String role   // currently unused
    ) {}

    public static record RegisterRequest(
            String firstName,
            String lastName,
            String phone,
            String email,
            String password,
            String address
    ) {}

    public static record LoginRequest(
            String email,
            String password
    ) {}

    public static record UpdateProfileRequest(
            String firstName,
            String lastName,
            String phone,
            String email,
            String address,
            String avatarPath
    ) {}

    public static record ChangePasswordRequest(
            String currentPassword,
            String newPassword
    ) {}

    public static record UserResponse(
            Long id,
            String firstName,
            String lastName,
            String phone,
            String email,
            String address,
            String avatarPath,
            Long updatedAt
    ) {
        public static UserResponse fromEntity(User u) {
            return new UserResponse(
                    u.getId(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.getPhone(),
                    u.getEmail(),
                    u.getAddress(),
                    u.getAvatarPath(),
                    u.getUpdatedAt()
            );
        }
    }
}
