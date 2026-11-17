package com.example.demo.dto;

/** JSON de entrada: {"username":"demo", "password":"demo123"}. */
public record LoginRequest(String username, String password) { }
