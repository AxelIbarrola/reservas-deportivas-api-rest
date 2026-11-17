package com.example.demo.controller;

import com.example.demo.model.Factura;
import com.example.demo.service.FacturaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/facturas")
public class FacturaController {
    private final FacturaService servicio;
    public FacturaController(FacturaService servicio) { this.servicio = servicio; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Factura crear(@RequestParam(required = false) Long idCliente, Authentication auth) {
        return servicio.crear(idCliente, auth);
    }
    @PostMapping("/{id}/detalles") @ResponseStatus(HttpStatus.CREATED)
    public Factura agregar(@PathVariable Long id, @RequestParam Long idProducto,
                           @RequestParam int cantidad, Authentication auth) {
        return servicio.agregarDetalle(id, idProducto, cantidad, auth);
    }
    @DeleteMapping("/{id}/detalles/{idDetalle}")
    public Factura quitar(@PathVariable Long id, @PathVariable Long idDetalle, Authentication auth) {
        return servicio.quitarDetalle(id, idDetalle, auth);
    }
    @PostMapping("/{id}/emitir")
    public Factura emitir(@PathVariable Long id, Authentication auth) {
        // No se aceptan descuentos enviados por el comprador.
        return servicio.emitir(id, auth);
    }

    // ADMIN consulta todas las facturas; USER solo ve las propias.
    @GetMapping
    public List<Factura> listar(Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin) throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.FORBIDDEN, "Solo el administrador puede listar todas las facturas.");
        return servicio.listar();
    }
    @GetMapping("/mias")
    public List<Factura> mias(Authentication auth) { return servicio.mias(auth.getName()); }
    @GetMapping("/{id}")
    public Factura buscar(@PathVariable Long id, Authentication auth) {
        return servicio.buscar(id, auth);
    }
}
