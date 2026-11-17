package com.tienda.backend.dto;

import jakarta.validation.constraints.*;

// para registrar una venta solo se ocupa el producto y cuantos se venden
// el precio y el total los calcula el backend
public record VentaRequestDTO(

        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El id del producto no es valido")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer cantidad
) {}
