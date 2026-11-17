package com.tienda.backend.service;

import com.tienda.backend.dto.ProductoRequestDTO;
import com.tienda.backend.dto.ProductoResponseDTO;
import com.tienda.backend.exception.ConflictoException;
import com.tienda.backend.exception.ResourceNotAvailable;
import com.tienda.backend.model.Producto;
import com.tienda.backend.repository.ProductoRepository;
import com.tienda.backend.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public ProductoService(ProductoRepository productoRepository, VentaRepository ventaRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> findAll() {
        return productoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        return toResponseDTO(buscarEntidadPorId(id));
    }

    @Transactional
    public ProductoResponseDTO saveProducto(ProductoRequestDTO dto) {
        Producto producto = new Producto();
        pasarDatos(dto, producto);
        return toResponseDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO updateProducto(Long id, ProductoRequestDTO dto) {
        Producto producto = buscarEntidadPorId(id);
        pasarDatos(dto, producto);
        return toResponseDTO(productoRepository.save(producto));
    }

    @Transactional
    public void deleteProducto(Long id) {
        Producto producto = buscarEntidadPorId(id);
        // si el producto ya se vendio no se puede borrar porque se pierden las ventas
        if (ventaRepository.existsByProductoId(id)) {
            throw new ConflictoException("No se puede eliminar el producto con ID " + id + " porque tiene ventas registradas");
        }
        productoRepository.delete(producto);
    }

    // --- metodos auxiliares ---

    // este lo usa tambien el servicio de ventas
    public Producto buscarEntidadPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotAvailable("No se encontró el producto con ID: " + id));
    }

    private void pasarDatos(ProductoRequestDTO dto, Producto producto) {
        producto.setNombre(dto.nombre().trim());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setStock(dto.stock());
    }

    private ProductoResponseDTO toResponseDTO(Producto p) {
        return new ProductoResponseDTO(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock());
    }
}
