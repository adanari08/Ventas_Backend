package com.tienda.backend.service;

import com.tienda.backend.dto.VentaRequestDTO;
import com.tienda.backend.dto.VentaResponseDTO;
import com.tienda.backend.exception.ResourceNotAvailable;
import com.tienda.backend.exception.StockInsuficienteException;
import com.tienda.backend.model.Producto;
import com.tienda.backend.model.Venta;
import com.tienda.backend.repository.ProductoRepository;
import com.tienda.backend.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                        ProductoService productoService) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.productoService = productoService;
    }

    @Transactional(readOnly = true)
    public List<VentaResponseDTO> findAll() {
        return ventaRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerPorId(Long id) {
        return toResponseDTO(buscarVenta(id));
    }

    @Transactional(readOnly = true)
    public List<VentaResponseDTO> ventasDeProducto(Long productoId) {
        productoService.buscarEntidadPorId(productoId); // para que de 404 si no existe
        return ventaRepository.findByProductoId(productoId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // registrar una venta: revisa el stock, lo descuenta y calcula el total
    @Transactional
    public VentaResponseDTO registrarVenta(VentaRequestDTO dto) {
        Producto producto = productoService.buscarEntidadPorId(dto.productoId());

        if (producto.getStock() < dto.cantidad()) {
            throw new StockInsuficienteException("Stock insuficiente para '" + producto.getNombre()
                    + "'. Disponible: " + producto.getStock() + ", solicitado: " + dto.cantidad());
        }

        producto.setStock(producto.getStock() - dto.cantidad());
        productoRepository.save(producto);

        Venta venta = new Venta();
        venta.setProducto(producto);
        venta.setCantidad(dto.cantidad());
        venta.setPrecioUnitario(producto.getPrecio());
        venta.setTotal(producto.getPrecio().multiply(BigDecimal.valueOf(dto.cantidad())));
        venta.setFecha(LocalDateTime.now());

        return toResponseDTO(ventaRepository.save(venta));
    }

    // actualizar venta: primero se regresa el stock de la venta vieja y luego se aplica la nueva
    @Transactional
    public VentaResponseDTO actualizarVenta(Long id, VentaRequestDTO dto) {
        Venta venta = buscarVenta(id);

        Producto anterior = venta.getProducto();
        anterior.setStock(anterior.getStock() + venta.getCantidad());
        productoRepository.save(anterior);

        Producto nuevo = productoService.buscarEntidadPorId(dto.productoId());
        if (nuevo.getStock() < dto.cantidad()) {
            throw new StockInsuficienteException("Stock insuficiente para '" + nuevo.getNombre()
                    + "'. Disponible: " + nuevo.getStock() + ", solicitado: " + dto.cantidad());
        }
        nuevo.setStock(nuevo.getStock() - dto.cantidad());
        productoRepository.save(nuevo);

        venta.setProducto(nuevo);
        venta.setCantidad(dto.cantidad());
        venta.setPrecioUnitario(nuevo.getPrecio());
        venta.setTotal(nuevo.getPrecio().multiply(BigDecimal.valueOf(dto.cantidad())));

        return toResponseDTO(ventaRepository.save(venta));
    }

    // eliminar (cancelar) venta: se regresa el stock al producto
    @Transactional
    public void eliminarVenta(Long id) {
        Venta venta = buscarVenta(id);
        Producto producto = venta.getProducto();
        producto.setStock(producto.getStock() + venta.getCantidad());
        productoRepository.save(producto);
        ventaRepository.delete(venta);
    }

    // --- metodos auxiliares ---

    private Venta buscarVenta(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotAvailable("No se encontró la venta con ID: " + id));
    }

    private VentaResponseDTO toResponseDTO(Venta v) {
        return new VentaResponseDTO(
                v.getId(),
                v.getProducto().getId(),
                v.getProducto().getNombre(),
                v.getCantidad(),
                v.getPrecioUnitario(),
                v.getTotal(),
                v.getFecha()
        );
    }
}
