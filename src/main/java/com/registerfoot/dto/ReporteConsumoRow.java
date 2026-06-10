package com.registerfoot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Fila plana para reportes. Es una clase JavaBean (no record) porque
 * JasperReports y POI acceden a las propiedades via getters get*.
 */
@Getter
@AllArgsConstructor
public class ReporteConsumoRow {
    private String numero;
    private String fecha;
    private String hora;
    private String empleado;
    private String documento;
    private String tipoComida;
    private String concesion;
    private BigDecimal valor;
}
