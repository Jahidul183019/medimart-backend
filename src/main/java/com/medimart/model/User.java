package com.medimart.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // DB primary key

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "address")
    private String address;

    @Column(name = "avatar_path")
    private String avatarPath;

    // NEW → CUSTOMER / ADMIN
    @Column(name = "role", nullable = false)
    private String role = "CUSTOMER";

    // NEW → Account creation time (epoch)
    @Column(name = "created_at")
    private Long createdAt = System.currentTimeMillis() / 1000;

    // Last profile update time (epoch)
    @Column(name = "updated_at")
    private Long updatedAt;

    public User() {}

    // full constructor
    public User(String firstName,
                String lastName,
                String phone,
                String email,
                String password,
                String address,
                String avatarPath,
                String role,
                Long createdAt,
                Long updatedAt) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.address = address;
        this.avatarPath = avatarPath;
        this.role = role != null ? role : "CUSTOMER";
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis() / 1000;
        this.updatedAt = updatedAt;
    }

    // simple signup constructor
    public User(String firstName,
                String lastName,
                String phone,
                String email,
                String password) {

        this(firstName, lastName, phone, email, password, null, null, "CUSTOMER",
                System.currentTimeMillis() / 1000, null);
    }

    /* =============================
       Getters & Setters
       ============================= */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
