package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.strategy.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional
public class FacturaService {
    private final FacturaRepository facturas;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;

    public FacturaService(FacturaRepository facturas, ClienteRepository clientes, ProductoRepository productos) {
        this.facturas = facturas;
        this.clientes = clientes;
        this.productos = productos;
    }

    private boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public Factura crear(Long idCliente, Authentication auth) {
        Cliente cliente;
        if (idCliente != null && esAdmin(auth)) {
            cliente = clientes.findById(idCliente).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente inexistente."));
        } else {
            if (idCliente != null) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Un cliente no puede comprar en nombre de otro.");
            cliente = clientes.findByUsuarioUsername(auth.getName()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta cuenta no tiene perfil de cliente."));
        }
        return facturas.save(new Factura(cliente));
    }

    /** Verifica propiedad para impedir IDOR: modificar facturas de otros usuarios por ID. */
    public Factura buscar(Long id, Authentication auth) {
        Factura factura = facturas.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura inexistente."));
        if (!esAdmin(auth) && (factura.getCliente().getUsuario() == null
                || !auth.getName().equals(factura.getCliente().getUsuario().getUsername()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta factura pertenece a otro cliente.");
        }
        return factura;
    }

    public Factura agregarDetalle(Long id, Long idProducto, int cantidad, Authentication auth) {
        Factura factura = buscar(id, auth);
        Producto producto = productos.findById(idProducto).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto inexistente."));
        if (cantidad > producto.getStock())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay suficiente stock.");
        factura.agregarDetalle(producto, cantidad);
        return facturas.saveAndFlush(factura);
    }

    public Factura quitarDetalle(Long id, Long idDetalle, Authentication auth) {
        Factura factura = buscar(id, auth);
        factura.quitarDetalle(idDetalle);
        return facturas.saveAndFlush(factura);
    }

    public Factura emitir(Long id, Authentication auth) {
        Factura factura = buscar(id, auth);
        // La estrategia se elige EN EL SERVIDOR según lo asignado al cliente.
        // El usuario no envía ni el tipo ni un porcentaje modificable.
        TipoDescuento tipo = factura.getCliente().getTipoDescuento();
        PoliticaDescuento politica = tipo == TipoDescuento.SIN_DESCUENTO
            ? new SinDescuento()
            : new DescuentoPorcentaje(java.math.BigDecimal.valueOf(tipo.getPorcentaje()));

        // Se valida stock ANTES de emitir. La operación completa es transaccional.
        if (factura.getDetalles().isEmpty())
            throw new IllegalArgumentException("La factura debe tener al menos un producto.");
        for (DetalleFactura detalle : factura.getDetalles()) {
            Producto p = detalle.getProducto();
            if (!p.isActivo() || p.getStock() < detalle.getCantidad())
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No hay stock suficiente del producto: " + p.getNombre());
        }
        factura.emitir(politica, tipo);
        for (DetalleFactura detalle : factura.getDetalles()) {
            Producto p = detalle.getProducto();
            p.setStock(p.getStock() - detalle.getCantidad());
        }
        return facturas.saveAndFlush(factura);
    }

    public List<Factura> listar() { return facturas.findAll(); }
    public List<Factura> mias(String username) {
        return facturas.findByClienteUsuarioUsernameOrderByIdFacturaDesc(username);
    }
}
