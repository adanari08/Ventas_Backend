package com.tienda.backend.exception;

// cuando se quiere vender mas de lo que hay en stock (400)
public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String message) {
        super(message);
    }
}
