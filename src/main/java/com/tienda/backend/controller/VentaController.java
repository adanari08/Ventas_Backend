package com.tienda.backend.controller;

import com.tienda.backend.dto.VentaRequestDTO;
import com.tienda.backend.dto.VentaResponseDTO;
import com.tienda.backend.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Registro de ventas de productos")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // LISTAR TODAS LAS VENTAS
    @Operation(summary = "Listar todas las ventas (mas recientes primero)")
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarVentas() {
        return ResponseEntity.ok(ventaService.findAll());
    }

    // OBTENER UNA VENTA
    @Operation(summary = "Obtener una venta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtenerVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    // VENTAS DE UN PRODUCTO
    @Operation(summary = "Listar las ventas de un producto")
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<VentaResponseDTO>> ventasPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(ventaService.ventasDeProducto(productoId));
    }

    // REGISTRAR VENTA (descuenta stock)
    @Operation(summary = "Registrar una venta")
    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrarVenta(@Valid @RequestBody VentaRequestDTO venta) {
        return new ResponseEntity<>(ventaService.registrarVenta(venta), HttpStatus.CREATED);
    }

    // ACTUALIZAR VENTA
    @Operation(summary = "Actualizar una venta")
    @PutMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> actualizarVenta(@PathVariable Long id,
                                                            @Valid @RequestBody VentaRequestDTO venta) {
        return ResponseEntity.ok(ventaService.actualizarVenta(id, venta));
    }

    // ELIMINAR VENTA (regresa el stock)
    @Operation(summary = "Eliminar una venta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }
}
