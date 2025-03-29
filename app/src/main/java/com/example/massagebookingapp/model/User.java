package com.example.massagebookingapp.model;

public class User {
    private String username;
    private String email;
    private String phone;

    private int role;

    public User() {
        // Üres konstruktor szükséges a Firestore számára
    }

    public User(String username, String email, String phone, int role) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public int getRole() {
        return role;
    }
}
