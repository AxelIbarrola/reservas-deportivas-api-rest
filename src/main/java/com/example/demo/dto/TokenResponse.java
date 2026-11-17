package com.example.demo.dto;

/** Datos que necesita el navegador tras iniciar sesión. */
public record TokenResponse(String token, String usuario, String rol) {}
