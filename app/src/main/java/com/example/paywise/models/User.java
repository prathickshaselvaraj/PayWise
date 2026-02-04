package com.example.paywise.models;

public class User {
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String profileImagePath;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public User() {}

    public User(String fullName, String email, String phone, String profileImagePath, String createdAt, String updatedAt) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.profileImagePath = profileImagePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}