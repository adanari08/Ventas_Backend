-- =============================================
--  Base de datos para el microservicio de tienda
--  Modulos: productos y ventas
-- =============================================

-- 1. crear la base de datos (correr esto solo una vez)
-- CREATE DATABASE tienda;

-- 2. conectarse a la base "tienda" y correr lo demas

DROP TABLE IF EXISTS ventas;
DROP TABLE IF EXISTS productos;

-- tabla de productos
CREATE TABLE productos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100)   NOT NULL,
    descripcion VARCHAR(255),
    precio      NUMERIC(10, 2) NOT NULL CHECK (precio > 0),
    stock       INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

-- tabla de ventas (cada venta es de un producto)
CREATE TABLE ventas (
    id              BIGSERIAL PRIMARY KEY,
    producto_id     BIGINT         NOT NULL REFERENCES productos (id),
    cantidad        INTEGER        NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10, 2) NOT NULL,
    total           NUMERIC(12, 2) NOT NULL,
    fecha           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ventas_producto ON ventas (producto_id);

-- datos de prueba
INSERT INTO productos (nombre, descripcion, precio, stock) VALUES
    ('Teclado Mecanico RGB', 'Teclado con switches azules', 1250.50, 15),
    ('Mouse Inalambrico',    'Mouse bluetooth 1600 dpi',     349.90, 30),
    ('Monitor 24 pulgadas',  'Monitor Full HD IPS',         2899.00,  8),
    ('Audifonos Gamer',      'Audifonos con microfono',      599.00, 20),
    ('Memoria USB 64GB',     NULL,                           159.00, 50);

-- algunas ventas (el stock de arriba ya esta descontado)
INSERT INTO ventas (producto_id, cantidad, precio_unitario, total) VALUES
    (1, 2, 1250.50, 2501.00),
    (2, 5,  349.90, 1749.50),
    (5, 3,  159.00,  477.00);

-- consultas para revisar
SELECT * FROM productos;

SELECT v.id, p.nombre, v.cantidad, v.precio_unitario, v.total, v.fecha
FROM ventas v
JOIN productos p ON p.id = v.producto_id
ORDER BY v.fecha DESC;
