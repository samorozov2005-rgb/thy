package com.example.taskmanager.dto;

public record JwtResponse(String token, String type) {
    public JwtResponse(String token) {
        this(token, "Bearer");
    }
}
