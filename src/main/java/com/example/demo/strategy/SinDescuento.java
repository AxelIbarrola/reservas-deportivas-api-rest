package com.example.demo.strategy;

import java.math.BigDecimal;

public class SinDescuento implements PoliticaDescuento {
    
    @Override 
    public BigDecimal calcularDescuento(BigDecimal subtotal) { 
        return BigDecimal.ZERO; 
    }
    
    @Override 
    public String getDescripcion() { 
        return "Sin descuento"; 
    }
}