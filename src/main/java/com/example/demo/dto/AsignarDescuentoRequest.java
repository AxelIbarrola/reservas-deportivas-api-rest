package com.example.demo.dto;

import com.example.demo.model.TipoDescuento;

/** El navegador solo envía una de las cuatro opciones conocidas. */
public record AsignarDescuentoRequest(TipoDescuento tipoDescuento) {}
