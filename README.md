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

### Tecnologías
- **Project OpenUBL** (XBuilder + XSender)
- **Jakarta XML Binding** (JAXB 4.0)
- **Apache XML Security** (Firma digital)
- **BouncyCastle** (Certificados)

## Solución de Problemas

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


