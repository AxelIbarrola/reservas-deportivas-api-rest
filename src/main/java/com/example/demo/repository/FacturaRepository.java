package com.example.demo.repository;

import com.example.demo.model.Factura;
import com.example.demo.model.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByClienteUsuarioUsernameOrderByIdFacturaDesc(String username);

    // Solo se cuentan compras finalizadas, nunca BORRADORES o ANULADAS.
    long countByCliente_IdClienteAndEstado(Long idCliente, EstadoFactura estado);
}
