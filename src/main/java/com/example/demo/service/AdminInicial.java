package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea o actualiza el administrador con los datos de application.properties. */
@Component
public class AdminInicial implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${demo.admin.username}") private String username;
    @Value("${demo.admin.password}") private String password;

    public AdminInicial(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Configurar demo.admin.username y demo.admin.password en application.properties");
        }

        User admin = users.findByUsername(username);
        if (admin == null) {
            // Primera ejecución: crear el usuario con rol ADMIN.
            users.save(new User(null, username, encoder.encode(password), "ADMIN"));
        } else if (!"ADMIN".equals(admin.getRole())) {
            // Un usuario común no puede convertirse en ADMIN por tener el mismo nombre.
            throw new IllegalStateException("El usuario configurado como administrador ya existe con otro rol");
        } else if (!encoder.matches(password, admin.getPassword())) {
            // Si el docente cambió la contraseña en properties, actualizarla en H2.
            admin.setPassword(encoder.encode(password));
            users.save(admin);
        }
    }
}
