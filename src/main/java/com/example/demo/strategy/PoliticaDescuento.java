package com.example.demo.strategy;

import java.math.BigDecimal;

// Strategy: cambia el algoritmo, pero conserva el contrato que utiliza Factura.
public interface PoliticaDescuento {
    BigDecimal calcularDescuento(BigDecimal subtotal);
    String getDescripcion();
}

