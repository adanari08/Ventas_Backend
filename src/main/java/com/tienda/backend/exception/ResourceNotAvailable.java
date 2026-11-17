package com.tienda.backend.exception;

// cuando no existe el registro que se busca (404)
public class ResourceNotAvailable extends RuntimeException {
    public ResourceNotAvailable(String message) {
        super(message);
    }
}
