package com.tienda.backend.repository;

import com.tienda.backend.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // ventas ordenadas de la mas nueva a la mas vieja
    List<Venta> findAllByOrderByFechaDesc();

    // ventas de un producto
    List<Venta> findByProductoId(Long productoId);

    boolean existsByProductoId(Long productoId);
}
