package com.tienda.backend.dto;

import java.math.BigDecimal;

// lo que regresamos al frontend
public record ProductoResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock
) {}
