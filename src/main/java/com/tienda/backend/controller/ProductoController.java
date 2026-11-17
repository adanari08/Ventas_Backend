package com.tienda.backend.controller;

import com.tienda.backend.dto.ProductoRequestDTO;
import com.tienda.backend.dto.ProductoResponseDTO;
import com.tienda.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "CRUD de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // LISTAR TODOS LOS PRODUCTOS
    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarProductos() {
        return ResponseEntity.ok(productoService.findAll());
    }

    // OBTENER UN PRODUCTO POR SU ID
    @Operation(summary = "Obtener un producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // CREAR PRODUCTO
    @Operation(summary = "Crear un producto")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO producto) {
        return new ResponseEntity<>(productoService.saveProducto(producto), HttpStatus.CREATED);
    }

    // ACTUALIZAR PRODUCTO
    @Operation(summary = "Actualizar un producto")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(@PathVariable Long id,
                                                                  @Valid @RequestBody ProductoRequestDTO producto) {
        return ResponseEntity.ok(productoService.updateProducto(id, producto));
    }

    // ELIMINAR PRODUCTO
    @Operation(summary = "Eliminar un producto (solo si no tiene ventas)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }
}
