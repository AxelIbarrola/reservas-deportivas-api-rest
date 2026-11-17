package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Producto;

// Spring crea la implementación: save, findAll y findById ya están disponibles.
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
