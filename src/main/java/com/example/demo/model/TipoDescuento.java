package com.example.demo.model;

/** Opciones comerciales: el ADMIN elige una, no el comprador. */
public enum TipoDescuento {
    SIN_DESCUENTO(0),
    CLIENTE_FRECUENTE(5),
    CLIENTE_PREFERENCIAL(10),
    CLIENTE_VIP(15);

    private final int porcentaje;

    TipoDescuento(int porcentaje) { this.porcentaje = porcentaje; }

    public int getPorcentaje() { return porcentaje; }
}
