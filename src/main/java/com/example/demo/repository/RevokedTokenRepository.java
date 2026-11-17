package com.example.demo.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    // Elimina revocaciones de tokens que ya caducaron.
    long deleteByExpiresAtBefore(Instant instant);
}
