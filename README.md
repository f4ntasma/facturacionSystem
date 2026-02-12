# Sistema de Facturación

Sistema de facturación desarrollado con Angular (frontend) y Spring Boot (backend).

## Inicio Rápido con Docker (Recomendado)

### Prerrequisitos
- [Docker](https://www.docker.com/get-started) instalado
- [Docker Compose](https://docs.docker.com/compose/install/) instalado

### Ejecutar la aplicación

#### Windows:
```bash
# Ejecutar desde la raíz del proyecto
scripts\start.bat
```

#### Linux/Mac:
```bash
# Dar permisos de ejecución (solo la primera vez)
chmod +x scripts/start.sh

# Ejecutar desde la raíz del proyecto
./scripts/start.sh
```

#### Comando directo:
```bash
docker-compose up --build
```

### Detener la aplicación

#### Windows:
```bash
scripts\stop.bat
```

#### Linux/Mac:
```bash
./scripts/stop.sh
```

#### Comando directo:
```bash
docker-compose down
```

### Acceso a la aplicación
- **Frontend (Angular)**: http://localhost:4200
- **Backend (Spring Boot)**: http://localhost:8080
- **Credenciales de prueba**: 
  - Usuario: `admin`
  - Contraseña: `admin`

## Desarrollo Local (Sin Docker)

### Backend (Spring Boot)

#### Prerrequisitos
- Java 17 o superior
- Maven 3.6+

#### Ejecutar backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend (Angular)

#### Prerrequisitos
- Node.js 18+ 
- npm

#### Ejecutar frontend
```bash
cd frontend
npm install
npm start
```

## Estructura del Proyecto

```
facturacion/
├── backend/                 # Spring Boot API
│   ├── src/
│   │   └── main/
│   │       ├── java/com/facturacion/
│   │       │   ├── auth/           # Autenticación JWT
│   │       │   ├── carrito/        # Gestión de carritos
│   │       │   ├── empresa/        # Gestión de empresas
│   │       │   ├── orden/          # Gestión de órdenes
│   │       │   ├── producto/       # Gestión de productos
│   │       │   └── sunat/          # 🆕 Facturación Electrónica SUNAT
│   │       │       ├── config/     # Configuración SUNAT
│   │       │       ├── controller/ # Endpoints REST
│   │       │       ├── dto/        # Request/Response DTOs
│   │       │       ├── exception/  # Manejo de errores
│   │       │       └── service/    # Lógica de negocio
│   │       └── resources/
│   │           ├── certificado/    # Certificados digitales
│   │           └── application.properties
│   ├── pom.xml
│   ├── Dockerfile
│   ├── SUNAT_INTEGRACION.md       # 📚 Documentación SUNAT
│   ├── SUNAT_EJEMPLOS.md          # 📝 Ejemplos de uso
│   └── SUNAT_RESUMEN.md           # ✅ Resumen técnico
├── frontend/                # Angular App
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── scripts/                 # Scripts de utilidad
│   ├── start.bat           # Iniciar en Windows
│   ├── start.sh            # Iniciar en Linux/Mac
│   ├── stop.bat            # Detener en Windows
│   ├── stop.sh             # Detener en Linux/Mac
│   └── generate-jwt.js     # Generador de JWT
├── docker-compose.yml       # Configuración Docker
└── README.md
```

## 🆕 Facturación Electrónica SUNAT

El sistema incluye integración completa con SUNAT para emisión de comprobantes electrónicos:

### Características
- ✅ Emisión de Facturas Electrónicas (Tipo 01)
- ✅ Emisión de Boletas de Venta (Tipo 03)
- ✅ Firma digital con certificado .pfx
- ✅ Envío automático a SUNAT
- ✅ Procesamiento de CDR (Constancia de Recepción)
- ✅ Estándar UBL 2.1
- ✅ Compatible con Java 17 y Spring Boot 3.2

### Tecnologías
- **Project OpenUBL** (XBuilder + XSender)
- **Jakarta XML Binding** (JAXB 4.0)
- **Apache XML Security** (Firma digital)
- **BouncyCastle** (Certificados)

### Documentación
- 📚 [Guía de Integración](backend/SUNAT_INTEGRACION.md)
- 📝 [Ejemplos de Uso](backend/SUNAT_EJEMPLOS.md)
- ✅ [Resumen Técnico](backend/SUNAT_RESUMEN.md)

### Endpoints SUNAT
```bash
POST /api/sunat/factura          # Emitir factura
POST /api/sunat/boleta           # Emitir boleta
GET  /api/sunat/consultar/{tipo}/{serie}/{numero}
GET  /api/sunat/certificado/info # Verificar certificado
GET  /api/sunat/test             # Test de configuración
```

### Configuración Rápida
1. Obtener certificado digital de SUNAT
2. Colocar en `backend/src/main/resources/certificado/certificado.pfx`
3. Configurar `application.properties`:
```properties
sunat.ruc=20000000001
sunat.certificado-path=classpath:certificado/certificado.pfx
sunat.certificado-password=tu_password
```

Ver documentación completa en [SUNAT_INTEGRACION.md](backend/SUNAT_INTEGRACION.md)

## Configuración

### Variables de Entorno (Docker)
Las configuraciones están predefinidas en `application-docker.properties`:
- Puerto backend: 8080
- Puerto frontend: 4200
- Base de datos: H2 en memoria
- JWT Secret: Configurado automáticamente

### Endpoints Principales

#### Autenticación
- `POST /api/auth/login` - Autenticación
- `POST /api/auth/register` - Registro de usuarios

#### Gestión
- `GET /api/empresas` - Listar empresas
- `GET /api/productos` - Listar productos
- `GET /api/ordenes` - Listar órdenes

#### Facturación Electrónica SUNAT 🆕
- `POST /api/sunat/factura` - Emitir factura electrónica
- `POST /api/sunat/boleta` - Emitir boleta de venta
- `GET /api/sunat/consultar/{tipo}/{serie}/{numero}` - Consultar estado
- `GET /api/sunat/certificado/info` - Info del certificado
- `GET /api/sunat/test` - Test de configuración

#### Desarrollo
- `GET /api/test` - Prueba de conectividad
- `GET /h2-console` - Consola de base de datos (desarrollo)
- `GET /swagger-ui.html` - Documentación API

## Solución de Problemas

### Error: Puerto en uso
```bash
# Verificar procesos usando los puertos
netstat -ano | findstr :8080
netstat -ano | findstr :4200

# Detener contenedores existentes
docker-compose down
```

### Error: Docker no encontrado
1. Instalar Docker Desktop desde https://www.docker.com/get-started
2. Reiniciar la terminal/comando
3. Verificar instalación: `docker --version`

### Error: Permisos en Linux/Mac
```bash
# Dar permisos a los scripts
chmod +x scripts/*.sh
```

### Reconstruir contenedores
```bash
# Forzar reconstrucción completa
docker-compose up --build --force-recreate
```

## Notas de Desarrollo

- El backend usa MariaDB en producción (puerto 3306) y H2 en desarrollo
- Spring Security con BCrypt para hashing de contraseñas
- Swagger UI en `/swagger-ui.html`
- API RESTful con documentación OpenAPI
- PrimeNG para componentes UI en Angular
- Responsive design
- Lazy loading de módulos
- JWT para autenticación con clave segura
- CORS configurado para desarrollo
- Hot reload habilitado en desarrollo local
- Perfiles de Spring Boot: `default`, `dev`, `docker`, `prod`
- 🆕 Integración completa con SUNAT para facturación electrónica
- 🆕 Firma digital de documentos con certificado .pfx
- 🆕 Estándar UBL 2.1 para comprobantes electrónicos

## 📚 Documentación Adicional

- [Integración SUNAT](backend/SUNAT_INTEGRACION.md) - Guía completa de facturación electrónica
- [Ejemplos SUNAT](backend/SUNAT_EJEMPLOS.md) - Ejemplos de requests y responses
- [Resumen Técnico SUNAT](backend/SUNAT_RESUMEN.md) - Detalles de implementación
- [Docker Setup](DOCKER_SETUP.md) - Configuración detallada de Docker
- [Funcionalidades](FUNCIONALIDADES_IMPLEMENTADAS.md) - Lista de features implementadas

