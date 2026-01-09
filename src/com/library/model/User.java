package com.library.model;

public class User {
    private String userId;
    private String name;
    private String password;
    private String securityQuestion; // SQUES
    private String securityAnswer;   // SANS

    // Constructor
    public User(String userId, String name, String password, String securityQuestion, String securityAnswer) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getSecurityQuestion() { return securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
}