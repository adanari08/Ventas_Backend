package com.tienda.backend;

import com.tienda.backend.dto.ProductoRequestDTO;
import com.tienda.backend.dto.ProductoResponseDTO;
import com.tienda.backend.exception.ConflictoException;
import com.tienda.backend.exception.ResourceNotAvailable;
import com.tienda.backend.model.Producto;
import com.tienda.backend.repository.ProductoRepository;
import com.tienda.backend.repository.VentaRepository;
import com.tienda.backend.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    private ProductoRepository productoRepository;
    private VentaRepository ventaRepository;
    private ProductoService productoService;

    @BeforeEach
    void preparar() {
        productoRepository = mock(ProductoRepository.class);
        ventaRepository = mock(VentaRepository.class);
        productoService = new ProductoService(productoRepository, ventaRepository);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Producto producto(Long id, String nombre) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(new BigDecimal("100.00"));
        p.setStock(5);
        return p;
    }

    @Test
    void listarProductos() {
        when(productoRepository.findAll()).thenReturn(List.of(producto(1L, "Mouse"), producto(2L, "Monitor")));
        assertEquals(2, productoService.findAll().size());
    }

    @Test
    void crearProducto() {
        ProductoResponseDTO creado = productoService.saveProducto(
                new ProductoRequestDTO("  Audifonos ", "Bluetooth", new BigDecimal("399.99"), 20));
        assertEquals("Audifonos", creado.nombre()); // le quita los espacios
        assertEquals(20, creado.stock());
    }

    @Test
    void actualizarProducto() {
        Producto p = producto(1L, "Mouse");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        ProductoResponseDTO r = productoService.updateProducto(1L,
                new ProductoRequestDTO("Mouse Gamer", null, new BigDecimal("150.00"), 8));
        assertEquals("Mouse Gamer", r.nombre());
        assertEquals(new BigDecimal("150.00"), r.precio());
    }

    @Test
    void buscarProductoQueNoExiste() {
        when(productoRepository.findById(50L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotAvailable.class, () -> productoService.obtenerPorId(50L));
    }

    @Test
    void noDejaBorrarProductoConVentas() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Mouse")));
        when(ventaRepository.existsByProductoId(1L)).thenReturn(true);
        assertThrows(ConflictoException.class, () -> productoService.deleteProducto(1L));
        verify(productoRepository, never()).delete(any());
    }

    @Test
    void borrarProductoSinVentas() {
        Producto p = producto(2L, "Monitor");
        when(productoRepository.findById(2L)).thenReturn(Optional.of(p));
        when(ventaRepository.existsByProductoId(2L)).thenReturn(false);
        productoService.deleteProducto(2L);
        verify(productoRepository).delete(p);
    }
}
