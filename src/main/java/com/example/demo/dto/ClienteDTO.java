package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.model.Cliente;
import com.example.demo.model.TipoDescuento;


// DTO: objeto que transporta únicamente los datos que queremos mostrar.
// No es una entidad JPA: no tiene tabla ni contiene password.
public class ClienteDTO {
    private Long idCliente;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDateTime fechaRegistro;
    private String direccion;
    private String telefono;
    private TipoDescuento tipoDescuento;
    private int porcentajeDescuento;
    private long comprasRealizadas;

    public ClienteDTO(Cliente cliente) {
        this(cliente, 0);
    }

    public ClienteDTO(Cliente cliente, long comprasRealizadas) {
        this.idCliente = cliente.getIdCliente();
        this.nombre = cliente.getNombre();
        this.apellido = cliente.getApellido();
        this.email = cliente.getEmail();
        this.fechaRegistro = cliente.getFechaRegistro();
        this.direccion = cliente.getDireccion();
        this.telefono = cliente.getTelefono();
        this.tipoDescuento = cliente.getTipoDescuento();
        this.porcentajeDescuento = tipoDescuento.getPorcentaje();
        this.comprasRealizadas = comprasRealizadas;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() { return telefono; }
    public TipoDescuento getTipoDescuento() { return tipoDescuento; }
    public int getPorcentajeDescuento() { return porcentajeDescuento; }
    public long getComprasRealizadas() { return comprasRealizadas; }

}
