# Facturacion

## Backend (Java + Spring Boot)
- Requisitos: Java 17+, Maven.
- Ejecutar: `cd backend` y `mvn spring-boot:run`.
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`.
- Base de datos en memoria H2 habilitada (`/h2-console`), credenciales por defecto en `application.yml`.

### Endpoints clave (prefijo `/api/v1`)
- Publico: `GET /info`, `POST /auth/register`, `POST /auth/login`, swagger y h2-console.
- Empresa: `GET /empresas/{ruc}`, `POST /empresas` (crea/actualiza empresa del usuario).
- Productos: `GET /productos`, `POST /productos`, `PUT /productos/{id}`, `DELETE /productos/{id}`, `GET /productos/autocomplete?q=...`.
- Carrito: `GET /carrito`, `POST /carrito/items`, `PUT /carrito/items/{itemId}?cantidad=3`, `DELETE /carrito/items/{itemId}`.
- Checkout/Ordenes/Pagos: `POST /checkout` (requiere `tipoComprobante` y `metodoPago`), `GET /ordenes`, `GET /ordenes/{id}`, `POST /ordenes/{id}/confirmar?referencia=...`.
- Catálogo público: `GET /catalogo?q=` devuelve productos activos (sin login).

### Pendientes/ideas
- Persistencia real en PostgreSQL/MySQL y migraciones.
- Blacklist/refresh de JWT para logout, recuperación de contraseñas.
- Generación de PDF (boleta/factura) y numeración oficial.
- Integración real de pagos (tarjeta/Yape) y webhooks.
- Control de stock avanzado (reservas, devoluciones), buscador con relevancia/multicliente.

### Implementación y configuración de Cors
- Se configuro el archivo dentro de configs, para configurar el archivos cors, para poder
conectar el fronten "Angular" con el backend "SpringBoot".
- Se implementó un componente de primeng para evitar tareas de creación de componentes desde
cero, para reducir algo de trabajo, aunque aún falta modificar y ajustar.