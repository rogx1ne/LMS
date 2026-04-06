package com.library.model;

public class UserProfile {
    private final String userId;
    private final String name;
    private final String email;
    private final long phoneNumber;
    private final String status;

    public UserProfile(String userId, String name, String email, long phoneNumber, String status) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public long getPhoneNumber() { return phoneNumber; }
    public String getStatus() { return status; }
}

