package com.tienda.backend;

import com.tienda.backend.dto.VentaRequestDTO;
import com.tienda.backend.dto.VentaResponseDTO;
import com.tienda.backend.exception.ResourceNotAvailable;
import com.tienda.backend.exception.StockInsuficienteException;
import com.tienda.backend.model.Producto;
import com.tienda.backend.model.Venta;
import com.tienda.backend.repository.ProductoRepository;
import com.tienda.backend.repository.VentaRepository;
import com.tienda.backend.service.ProductoService;
import com.tienda.backend.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// pruebas de la logica de ventas (sin base de datos, con mocks)
class VentaServiceTest {

    private ProductoRepository productoRepository;
    private VentaRepository ventaRepository;
    private VentaService ventaService;
    private Producto teclado;

    @BeforeEach
    void preparar() {
        productoRepository = mock(ProductoRepository.class);
        ventaRepository = mock(VentaRepository.class);
        ProductoService productoService = new ProductoService(productoRepository, ventaRepository);
        ventaService = new VentaService(ventaRepository, productoRepository, productoService);

        teclado = new Producto();
        teclado.setId(1L);
        teclado.setNombre("Teclado");
        teclado.setPrecio(new BigDecimal("250.50"));
        teclado.setStock(10);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(teclado));
        // el save regresa lo mismo que recibe
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void registrarVentaDescuentaStockYCalculaTotal() {
        VentaResponseDTO venta = ventaService.registrarVenta(new VentaRequestDTO(1L, 3));

        assertEquals(7, teclado.getStock());
        assertEquals(new BigDecimal("751.50"), venta.total());
        assertEquals(new BigDecimal("250.50"), venta.precioUnitario());
        assertEquals("Teclado", venta.nombreProducto());
    }

    @Test
    void noDejaVenderMasDelStock() {
        assertThrows(StockInsuficienteException.class,
                () -> ventaService.registrarVenta(new VentaRequestDTO(1L, 11)));
        assertEquals(10, teclado.getStock()); // el stock no cambia
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void ventaDeProductoQueNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotAvailable.class,
                () -> ventaService.registrarVenta(new VentaRequestDTO(99L, 1)));
    }

    @Test
    void eliminarVentaRegresaElStock() {
        Venta venta = new Venta();
        venta.setId(5L);
        venta.setProducto(teclado);
        venta.setCantidad(4);
        teclado.setStock(6);
        when(ventaRepository.findById(5L)).thenReturn(Optional.of(venta));

        ventaService.eliminarVenta(5L);

        assertEquals(10, teclado.getStock());
        verify(ventaRepository).delete(venta);
    }

    @Test
    void actualizarVentaAjustaElStock() {
        Venta venta = new Venta();
        venta.setId(5L);
        venta.setProducto(teclado);
        venta.setCantidad(2);
        teclado.setStock(8); // ya se habian vendido 2
        when(ventaRepository.findById(5L)).thenReturn(Optional.of(venta));

        VentaResponseDTO actualizada = ventaService.actualizarVenta(5L, new VentaRequestDTO(1L, 5));

        assertEquals(5, teclado.getStock()); // 8 + 2 - 5
        assertEquals(5, actualizada.cantidad());
        assertEquals(new BigDecimal("1252.50"), actualizada.total());
    }
}
