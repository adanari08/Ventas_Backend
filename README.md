# tienda-backend

Microservicio hecho con **Spring Boot** y **PostgreSQL** para una tienda.
Tiene dos modulos: **productos** y **ventas**.

Basado en el ejemplo del profe: https://github.com/BanderoChinoZF/springboot_backend_store

## Tecnologias
- Java 21
- Spring Boot 4 (Web, Data JPA, Validation, Security, Actuator)
- PostgreSQL
- Swagger (springdoc)

## Como correrlo

1. Crear la base de datos en PostgreSQL y correr el script `database/tienda.sql`
   (crea las tablas y mete datos de prueba):
   ```
   psql -U postgres -c "CREATE DATABASE tienda"
   psql -U postgres -d tienda -f database/tienda.sql
   ```
2. Revisar los datos de conexion en `src/main/resources/application.yaml`
   (por defecto usa `localhost:5432/tienda`, usuario `postgres`, contraseña `postgres`).
   Tambien se pueden mandar con variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
3. Correr el proyecto:
   ```
   ./mvnw spring-boot:run        (en windows: mvnw.cmd spring-boot:run)
   ```
   o darle Run a `TiendaApplication` desde IntelliJ.
4. Abrir Swagger: http://localhost:8000/swagger-ui.html

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
- Pruebas unitarias en `src/test` (JUnit + Mockito, no ocupan base de datos):
  ```
  ./mvnw test
  ```
- Coleccion de Postman en `postman/tienda.postman_collection.json` para probar todos los endpoints.

## Subir a Render
Crear un Web Service con Docker o con Java y poner las variables:
`DB_URL` (formato `jdbc:postgresql://host:5432/basededatos`), `DB_USER`, `DB_PASSWORD`
y `FRONTEND_URL` (la url del frontend en vue, para el CORS).
