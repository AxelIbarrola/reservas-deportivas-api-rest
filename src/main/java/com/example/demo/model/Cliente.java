package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

// JPA guarda los objetos de esta clase en una tabla.
@Entity
public class Cliente {
    
    // La base genera el ID: ya no lo asignamos a mano.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDateTime fechaRegistro;
    private String direccion;
    private String telefono;

    // El descuento lo asigna ADMIN; en una nueva cuenta no hay beneficio.
    @Enumerated(EnumType.STRING)
    // Nullable para que ddl-auto=update pueda agregar el campo a una H2 existente.
    private TipoDescuento tipoDescuento = TipoDescuento.SIN_DESCUENTO;
    // Vínculo entre el cliente comercial y su cuenta autenticada.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    @JsonIgnore
    private User usuario;


    // Jackson crea el objeto vacío y luego completa sus setters desde el JSON.
    @JsonCreator
    public Cliente(){}


    public Cliente(Long idCliente, String nombre, String apellido, String email, LocalDateTime fechaRegistro,
            String direccion, String telefono, User usuario) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.direccion = direccion;
        this.telefono = telefono;
        this.usuario = usuario;
    }


    public Long getIdCliente() {
        return idCliente;
    }


    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }


    public String getNombre() {
        return nombre;
    }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getApellido() {
        return apellido;
    }


    public void setApellido(String apellido) {
        this.apellido = apellido;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }


    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }


    public String getDireccion() {
        return direccion;
    }


    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }


    public String getTelefono() {
        return telefono;
    }


    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }


    public TipoDescuento getTipoDescuento() {
        return tipoDescuento == null ? TipoDescuento.SIN_DESCUENTO : tipoDescuento;
    }
    public void setTipoDescuento(TipoDescuento tipoDescuento) {
        this.tipoDescuento = tipoDescuento;
    }

    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
}
