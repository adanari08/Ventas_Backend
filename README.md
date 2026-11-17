# Ventas_Backend

Microservicio hecho con **Spring Boot** y **PostgreSQL** para una tienda.
Tiene dos modulos: **productos** y **ventas**.

Repositorio del frontend: https://github.com/adanari08/Ventas_Frontend

## Tecnologias
- Java 17 o mas nuevo
- Spring Boot 4 (Web, Data JPA, Validation, Security, Actuator)
- PostgreSQL
- Swagger (springdoc)

## Como correrlo
1. Crear la base de datos `tienda` en PostgreSQL y correr el script `database/tienda.sql`
   (crea las tablas y mete datos de prueba).
2. Revisar los datos de conexion en `src/main/resources/application.yaml`
   (url con el puerto de tu postgres, usuario y contraseña).
   Tambien se pueden mandar con variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
3. Correr el proyecto:
   ```
   .\mvnw.cmd clean spring-boot:run
   ```
4. La API queda en http://localhost:8000

## Endpoints

### Productos  `/api/productos`
| Metodo | Ruta | Que hace |
|---|---|---|
| GET | `/api/productos` | lista todos los productos |
| GET | `/api/productos/{id}` | busca un producto |
| POST | `/api/productos` | crea un producto |
| PUT | `/api/productos/{id}` | actualiza un producto |
| DELETE | `/api/productos/{id}` | elimina un producto (si no tiene ventas) |

Ejemplo de body:
```json
{ "nombre": "Teclado", "descripcion": "Mecanico", "precio": 1250.50, "stock": 15 }
```

### Ventas  `/api/ventas`
| Metodo | Ruta | Que hace |
|---|---|---|
| GET | `/api/ventas` | lista todas las ventas |
| GET | `/api/ventas/{id}` | busca una venta |
| GET | `/api/ventas/producto/{productoId}` | ventas de un producto |
| POST | `/api/ventas` | registra una venta (descuenta el stock) |
| PUT | `/api/ventas/{id}` | modifica una venta (ajusta el stock) |
| DELETE | `/api/ventas/{id}` | elimina una venta (regresa el stock) |

Ejemplo de body:
```json
{ "productoId": 1, "cantidad": 2 }
```
El precio unitario y el total los calcula el backend con el precio del producto.

### Reglas
- No se puede vender mas de lo que hay en stock (regresa 400).
- No se puede borrar un producto que ya tiene ventas (regresa 409).
- Si no existe el producto o la venta regresa 404.

## Pruebas
- **Pruebas de endpoints:** con el backend encendido correr
  ```
  powershell -ExecutionPolicy Bypass -File .\postman\pruebas-endpoints.ps1
  ```
  Hace 21 pruebas y guarda los resultados en `postman/resultados-pruebas.csv`.
  Las mismas pruebas estan en la coleccion de Postman `postman/tienda.postman_collection.json`.
- **Pruebas unitarias** (JUnit + Mockito, no ocupan base de datos):
  ```
  .\mvnw.cmd clean test
  ```
