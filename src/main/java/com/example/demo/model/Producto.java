package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.math.BigDecimal;

// JPA guarda los objetos de esta clase en una tabla.
@Entity
public class Producto {

    // La base genera el ID: ya no lo asignamos a mano.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;
    private String nombre;
    private String descripcion;
    // Dos decimales para los importes monetarios almacenados.
    @Column(precision = 19, scale = 2)
    private BigDecimal precio;
    private Integer stock;
    private boolean activo;

    // Jackson crea el objeto vacío y luego completa sus setters desde el JSON.
    @JsonCreator
    public Producto(){}

    
    public Producto(Long idProducto, String nombre, String descripcion, BigDecimal precio, Integer stock,
            boolean activo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.activo = activo;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    
}
