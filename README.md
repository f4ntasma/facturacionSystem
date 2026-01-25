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
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                # Angular App
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── scripts/                 # Scripts de utilidad
│   ├── start.bat           # Iniciar en Windows
│   ├── start.sh            # Iniciar en Linux/Mac
│   ├── stop.bat            # Detener en Windows
│   └── stop.sh             # Detener en Linux/Mac
├── docker-compose.yml       # Configuración Docker
└── README.md
```

## Configuración

### Variables de Entorno (Docker)
Las configuraciones están predefinidas en `application-docker.properties`:
- Puerto backend: 8080
- Puerto frontend: 4200
- Base de datos: H2 en memoria
- JWT Secret: Configurado automáticamente

### Endpoints Principales
- `POST /api/auth/login` - Autenticación
- `GET /api/test` - Prueba de conectividad
- `GET /h2-console` - Consola de base de datos (desarrollo)

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

- El backend usa H2 como base de datos en memoria
- JWT para autenticación
- CORS configurado para desarrollo
- Hot reload habilitado en desarrollo local
- Perfiles de Spring Boot: `default` y `docker`

