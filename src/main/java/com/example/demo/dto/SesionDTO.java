package com.example.demo.dto;

import java.util.List;

/** La respuesta deliberadamente NO devuelve la contraseña ni un JWT. */
public record SesionDTO(String usuario, List<String> roles, String mensaje) { }
