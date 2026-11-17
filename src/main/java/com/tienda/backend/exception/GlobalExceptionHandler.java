package com.tienda.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // metodo para no repetir lo mismo en cada handler
    private ResponseEntity<ErrorResponse> crearRespuesta(HttpStatus status, String error, String mensaje,
                                                         HttpServletRequest request, Map<String, String> detalles) {
        ErrorResponse respuesta = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                mensaje,
                request.getRequestURI(),
                detalles
        );
        return ResponseEntity.status(status).body(respuesta);
    }

    // 1. errores de validacion (@NotBlank, @Positive, etc) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );
        return crearRespuesta(HttpStatus.BAD_REQUEST, "Error de validación", null, request, errores);
    }

    // 2. JSON mal escrito o tipo de dato incorrecto -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "Cuerpo JSON inválido o mal formado",
                ex.getMostSpecificCause().getMessage(), request, null);
    }

    // 3. cuando mandan un id que no es numero, ej. /api/productos/abc -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "El valor '" + ex.getValue() + "' no es válido para " + ex.getName(), request, null);
    }

    // 4. no hay stock suficiente para la venta -> 400
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> manejarStock(StockInsuficienteException ex, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "Stock insuficiente", ex.getMessage(), request, null);
    }

    // 5. no existe el producto o la venta -> 404
    @ExceptionHandler(ResourceNotAvailable.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(ResourceNotAvailable ex, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request, null);
    }

    // 6. ruta que no existe -> 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRutaNoExiste(NoResourceFoundException ex, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.NOT_FOUND, "Ruta no encontrada", null, request, null);
    }

    // 7. producto con ventas que se quiere borrar -> 409
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> manejarConflicto(ConflictoException ex, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), request, null);
    }

    // 8. cualquier otro error -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null, request, null);
    }
}
