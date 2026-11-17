package com.example.demo.service;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository productos;
    public ProductoService(ProductoRepository productos) { this.productos = productos; }

    private void validar(Producto datos) {
        if (datos == null || datos.getNombre() == null || datos.getNombre().isBlank()
            || datos.getPrecio() == null || datos.getPrecio().signum() <= 0
            || datos.getPrecio().stripTrailingZeros().scale() > 2
            || datos.getPrecio().compareTo(new BigDecimal("9999999999.99")) > 0
            || datos.getStock() == null || datos.getStock() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Indicá nombre, precio positivo con hasta 2 decimales y stock no negativo.");
    }
    public Producto crear(Producto datos) {
        validar(datos);
        return productos.save(new Producto(null, datos.getNombre(), datos.getDescripcion(),
                                           datos.getPrecio(), datos.getStock(), true));
    }
    public Producto modificar(Long id, Producto datos) {
        validar(datos);
        Producto actual = buscar(id);
        actual.setNombre(datos.getNombre());
        actual.setDescripcion(datos.getDescripcion());
        actual.setPrecio(datos.getPrecio());
        actual.setStock(datos.getStock());
        actual.setActivo(datos.isActivo());
        return productos.save(actual);
    }
    // Baja lógica: conserva el producto referido por facturas históricas.
    public void desactivar(Long id) {
        Producto actual = buscar(id);
        actual.setActivo(false);
        productos.save(actual);
    }
    public List<Producto> listar() { return productos.findAll(); }
    public Producto buscar(Long id) {
        return productos.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto inexistente."));
    }
}
