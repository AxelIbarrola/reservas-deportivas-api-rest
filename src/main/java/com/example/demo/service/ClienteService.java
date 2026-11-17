package com.example.demo.service;

import com.example.demo.dto.RegistroRequest;
import com.example.demo.model.Cliente;
import com.example.demo.model.EstadoFactura;
import com.example.demo.model.TipoDescuento;
import com.example.demo.dto.ClienteDTO;
import com.example.demo.repository.FacturaRepository;
import com.example.demo.model.User;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository clientes;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final FacturaRepository facturas;

    public ClienteService(ClienteRepository clientes, UserRepository users,
                          PasswordEncoder encoder, FacturaRepository facturas) {
        this.clientes = clientes;
        this.users = users;
        this.encoder = encoder;
        this.facturas = facturas;
    }

    @Transactional
    public Cliente registrar(RegistroRequest datos) {
        if (datos == null || vacio(datos.username()) || vacio(datos.nombre()) || vacio(datos.email())
                || datos.password() == null || datos.password().length() < 8
                || datos.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Indicá usuario, nombre, email y contraseña de 8 caracteres como mínimo (máximo 72 bytes).");
        }
        if (users.existsByUsername(datos.username().trim()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe.");
        if (clientes.existsByEmailIgnoreCase(datos.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado.");

        User user = users.save(new User(null, datos.username().trim(),
                encoder.encode(datos.password()), "USER"));
        Cliente cliente = new Cliente(null, datos.nombre().trim(), datos.apellido(),
                datos.email().trim(), LocalDateTime.now(), datos.direccion(), datos.telefono(), user);
        return clientes.save(cliente);
    }

    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }

    public List<ClienteDTO> listar() {
        // En esta demo pequeña, una consulta COUNT por cliente es más fácil de leer.
        return clientes.findAll().stream().map(this::resumen).toList();
    }

    public ClienteDTO resumen(Cliente cliente) {
        long emitidas = facturas.countByCliente_IdClienteAndEstado(
            cliente.getIdCliente(), EstadoFactura.EMITIDA);
        return new ClienteDTO(cliente, emitidas);
    }

    @Transactional
    public ClienteDTO asignarDescuento(Long id, TipoDescuento tipo) {
        if (tipo == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccioná una estrategia válida.");
        Cliente cliente = buscar(id);
        cliente.setTipoDescuento(tipo);
        return resumen(clientes.save(cliente));
    }
    public Cliente buscar(Long id) {
        return clientes.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente inexistente."));
    }
    public Cliente miPerfil(String username) {
        return clientes.findByUsuarioUsername(username).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay cliente para esta cuenta."));
    }
}
