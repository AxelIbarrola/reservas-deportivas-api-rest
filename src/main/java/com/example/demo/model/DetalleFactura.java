package com.example.demo.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

/** 
 * DetalleFactura representa un renglón de la factura: 
 * un producto, su cantidad y el precio unitario aplicado. 
 * La lista de todos esos renglones está en Factura.
 */

// JPA guarda los objetos de esta clase en una tabla.
@Entity
public class DetalleFactura {

    // La base genera el ID: ya no lo asignamos a mano.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_factura")
    @JsonBackReference
    private Factura factura;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;
    private Integer cantidad;
    // Conservamos el precio de esta venta aunque luego cambie el catálogo.
    @Column(precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    protected DetalleFactura() {}

    public DetalleFactura(Factura factura, Producto producto, int cantidad){
        this.validar(factura, producto, cantidad);
        this.factura = factura;
        this.producto = producto;        
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
    }

    public void validar(Factura factura, Producto producto, int cantidad){
        if (factura == null)
            throw new IllegalArgumentException("Es necesario una factura.");
        if (producto == null)
            throw new IllegalArgumentException("Es necesario un producto.");
        if (cantidad <= 0 || cantidad > 10000) 
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y 10000");
    }

    public BigDecimal calcularSubtotal() { 
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    // El getter expone el cálculo como una propiedad "subtotal" en el JSON.
    public BigDecimal getSubtotal() {
        return calcularSubtotal();
    }

    // Este getter muestra el precio que quedó guardado en el renglón.
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

}
