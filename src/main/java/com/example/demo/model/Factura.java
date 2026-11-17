package com.example.demo.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import com.example.demo.strategy.PoliticaDescuento;
import com.fasterxml.jackson.annotation.JsonManagedReference;

// JPA guarda los objetos de esta clase en una tabla.
@Entity
public class Factura {

    // La base genera el ID: ya no lo asignamos a mano.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFactura;
    // Muchas facturas pueden referenciar al mismo cliente.
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEmision;
    @Enumerated(EnumType.STRING)
    private EstadoFactura estado;
    private String receptorNombre;
    private String receptorEmail;
    private String receptorDireccion;
    // Dos decimales para los importes monetarios almacenados.
    @Column(precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    // Dos decimales para los importes monetarios almacenados.
    @Column(precision = 19, scale = 2)
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;
    // Dos decimales para los importes monetarios almacenados.
    @Column(precision = 19, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    private String politicaAplicada;

    // Fotografía del beneficio al emitir: NO cambia si ADMIN modifica al cliente.
    @Enumerated(EnumType.STRING)
    private TipoDescuento tipoDescuento;
    private Integer porcentajeDescuento;


    // Guardar una factura guarda sus renglones; retirarlos los elimina.
    // EAGER carga la lista para devolver el JSON sin añadir DTO ni mapeadores.
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("idDetalle ASC")
    @JsonManagedReference
    private List<DetalleFactura> detalles = new ArrayList<>();


    // Hibernate necesita un constructor sin argumentos para reconstruir la entidad.
    protected Factura() {}

    public Factura(Cliente cliente){
        if (cliente == null)
            throw new IllegalArgumentException("Es necesario un cliente para la factura.");
        this.cliente = cliente;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoFactura.BORRADOR;
        this.receptorNombre = this.cliente.getApellido() + ", " + this.cliente.getNombre();
        this.receptorEmail = this.cliente.getEmail();
        this.receptorDireccion = this.cliente.getDireccion();
    }

   public void agregarDetalle(Producto producto, int cantidad) {
        comprobarBorrador();
        if (cantidad <= 0 || cantidad > 10000) 
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y 10000.");
        if (producto == null || !producto.isActivo()) 
            throw new IllegalArgumentException("El producto no está disponible.");

        // Comprobar si el producto ya está incluido en esta factura.
        boolean existeProducto = detalles.stream()
            .anyMatch(dtl -> dtl.getProducto().getIdProducto().equals(producto.getIdProducto()));
        if (existeProducto) {
            throw new IllegalArgumentException("El elemento existe en la lista.");
        }

        if (detalles.size() >= 100) 
            throw new IllegalArgumentException("Máximo 100 productos distintos por factura.");

        DetalleFactura detalle = new DetalleFactura(this, producto, cantidad);
        this.detalles.add(detalle);
        recalcularBorrador();
    }

    public void quitarDetalle(Long idDetalle) {
        comprobarBorrador();
        DetalleFactura detalle = detalles.stream()
                                  .filter(d -> idDetalle.equals(d.getIdDetalle())).findFirst()
                                  .orElseThrow(() -> new IllegalArgumentException("El renglón no pertenece a esta factura."));
        detalles.remove(detalle);
        recalcularBorrador(); 
    }

    private void comprobarBorrador() {
        if (estado != EstadoFactura.BORRADOR)
            throw new IllegalArgumentException("La factura emitida no se puede modificar.");
    }

    private void recalcularBorrador() {
        subtotal = sumarDetalles();
        total = subtotal; // El descuento se aplica recién al emitir.
    }

    private BigDecimal sumarDetalles() {
        return detalles.stream().map(DetalleFactura::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void emitir(PoliticaDescuento politica, TipoDescuento tipo) {
        comprobarBorrador();
        if (detalles.isEmpty()) 
            throw new IllegalArgumentException("La factura debe tener al menos un renglón.");
        if (politica == null || tipo == null)
            throw new IllegalArgumentException("La política es obligatoria.");

        this.subtotal = this.sumarDetalles(); 
        // La estrategia devuelve el importe del descuento, no el total a pagar.
        this.descuentoAplicado = politica.calcularDescuento(subtotal);
        this.total = this.subtotal.subtract(this.descuentoAplicado);
        this.politicaAplicada = politica.getDescripcion();
        this.tipoDescuento = tipo;
        this.porcentajeDescuento = tipo.getPorcentaje();
        this.fechaEmision = LocalDateTime.now();
        this.receptorNombre = cliente.getNombre() + " " + cliente.getApellido();
        this.receptorEmail = cliente.getEmail(); 
        this.receptorDireccion = cliente.getDireccion();
        this.estado = EstadoFactura.EMITIDA;
    }   


    public Long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Long idFactura) {
        this.idFactura = idFactura;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public String getReceptorNombre() {
        return receptorNombre;
    }

    public String getReceptorEmail() {
        return receptorEmail;
    }

    public String getReceptorDireccion() {
        return receptorDireccion;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDescuentoAplicado() {
        return descuentoAplicado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getPoliticaAplicada() {
        return politicaAplicada;
    }

    public TipoDescuento getTipoDescuento() { return tipoDescuento; }
    public Integer getPorcentajeDescuento() { return porcentajeDescuento; }


    public List<DetalleFactura> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

}
