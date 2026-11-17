package com.example.demo.controller;

import com.example.demo.model.Producto;
import com.example.demo.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService servicio;
    public ProductoController(ProductoService servicio) { this.servicio = servicio; }

    @GetMapping
    public List<Producto> listar() { return servicio.listar(); }
    @GetMapping("/{id}")
    public Producto buscar(@PathVariable Long id) { return servicio.buscar(id); }
    // Altas, cambios y bajas solo ADMIN: véase WebSecurityConfig.
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@RequestBody Producto datos) { return servicio.crear(datos); }
    @PutMapping("/{id}")
    public Producto modificar(@PathVariable Long id, @RequestBody Producto datos) {
        return servicio.modificar(id, datos);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { servicio.desactivar(id); }
}
