package com.example.demo.dto;

/** Datos públicos de registro: el navegador no puede elegir ni ID ni rol. */
public record RegistroRequest(String username, String password, String nombre,
                              String apellido, String email, String direccion, String telefono) {}
