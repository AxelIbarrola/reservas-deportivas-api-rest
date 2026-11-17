package com.example.demo.controller;

import com.example.demo.dto.ClienteDTO;
import com.example.demo.dto.RegistroRequest;
import com.example.demo.dto.AsignarDescuentoRequest;
import org.springframework.http.HttpStatus;
import com.example.demo.model.Cliente;
import com.example.demo.service.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService servicio;
    public ClienteController(ClienteService servicio) { this.servicio = servicio; }

    // La cuenta USER ve únicamente su propio perfil.
    @GetMapping("/me")
    public ClienteDTO miPerfil(Authentication auth) {
        return servicio.resumen(servicio.miPerfil(auth.getName()));
    }

    // Creación administrativa alternativa al registro público /api/auth/signup.
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ClienteDTO crear(@RequestBody RegistroRequest datos) {
        return new ClienteDTO(servicio.registrar(datos));
    }

    // GET /clientes y GET /clientes/{id} están reservados a ADMIN en WebSecurityConfig.
    @GetMapping
    public List<ClienteDTO> listar() { return servicio.listar(); }
    @GetMapping("/{id}")
    public ClienteDTO buscar(@PathVariable Long id) { return servicio.resumen(servicio.buscar(id)); }

    // Solo ADMIN (regla en WebSecurityConfig). USER no puede asignarse beneficios.
    @PutMapping("/{id}/descuento")
    public ClienteDTO asignarDescuento(@PathVariable Long id,
                                       @RequestBody AsignarDescuentoRequest datos) {
        if (datos == null) throw new IllegalArgumentException("Falta el tipo de descuento.");
        return servicio.asignarDescuento(id, datos.tipoDescuento());
    }
}
