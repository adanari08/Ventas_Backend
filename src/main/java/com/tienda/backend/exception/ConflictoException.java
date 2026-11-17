package com.tienda.backend.exception;

// por ejemplo cuando se quiere borrar un producto que ya tiene ventas (409)
public class ConflictoException extends RuntimeException {
    public ConflictoException(String message) {
        super(message);
    }
}
