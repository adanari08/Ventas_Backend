package com.tienda.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResponseDTO(
        Long id,
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal total,
        LocalDateTime fecha
) {}
