package com.example.demo.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DescuentoPorcentaje implements PoliticaDescuento {
    
    private final BigDecimal porcentaje;
    
    public DescuentoPorcentaje(BigDecimal porcentaje) {
        if (porcentaje == null || porcentaje.signum() < 0 || porcentaje.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        this.porcentaje = porcentaje;
    }
    
    @Override 
    public BigDecimal calcularDescuento(BigDecimal subtotal) {
        return subtotal.multiply(porcentaje)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getDescripcion() {
        return "Descuento del " + porcentaje.toPlainString() + "%";
    }
}

