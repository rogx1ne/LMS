package com.library.model;

public class User {
    private String userId;
    private String name;
    private String password;
    private String email;
    private long phoneNumber;
    private String status;

    // Constructor
    public User(String userId, String name, String password, String email, long phoneNumber) {
        this(userId, name, password, email, phoneNumber, "ACTIVE");
    }

    public User(String userId, String name, String password, String email, long phoneNumber, String status) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public long getPhoneNumber() { return phoneNumber; }
    public String getStatus() { return status; }
}
