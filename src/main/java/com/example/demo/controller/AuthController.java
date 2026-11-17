package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.ClienteService;
import com.example.demo.service.TokenRevocationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager manager;
    private final UserRepository users;
    private final ClienteService clientes;
    private final JwtUtil jwtUtil;
    private final TokenRevocationService revocacion;

    public AuthController(AuthenticationManager manager, UserRepository users,
                          ClienteService clientes, JwtUtil jwtUtil, TokenRevocationService revocacion) {
        this.manager = manager;
        this.users = users;
        this.clientes = clientes;
        this.jwtUtil = jwtUtil;
        this.revocacion = revocacion;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDTO registrarse(@RequestBody RegistroRequest datos) {
        return new ClienteDTO(clientes.registrar(datos));
    }

    @PostMapping("/signin")
    public TokenResponse ingresar(@RequestBody LoginRequest datos) {
        Authentication auth = manager.authenticate(
            new UsernamePasswordAuthenticationToken(datos.username(), datos.password()));
        User user = users.findByUsername(auth.getName());
        return new TokenResponse(jwtUtil.generateToken(auth.getName()), user.getUsername(), user.getRole());
    }

    @GetMapping("/me")
    public SesionDTO miSesion(Authentication auth) {
        return new SesionDTO(auth.getName(),
            auth.getAuthorities().stream().map(a -> a.getAuthority()).toList(), "Autenticado");
    }

    @PostMapping("/logout")
    public String salir(@RequestHeader("Authorization") String authorization) {
        // Solo llega aquí cuando el filtro JWT autenticó la petición.
        revocacion.revoke(authorization.substring(7));
        return "JWT revocado correctamente.";
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String errorCredenciales() { return "Usuario o contraseña incorrectos."; }
}
