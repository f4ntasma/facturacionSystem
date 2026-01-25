# Instrucciones para Ejecutar Factullama Corretamente 100% real no fake.

## Objetivo
Ejecutar el sistema de facturación sin necesidad de instalar Java/JDK manualmente.

## Pasos para tener todo goti

### 1. Instalar Docker
- **Windows**: Descargar e instalar Docker Desktop desde https://www.docker.com/products/docker-desktop/
- **Linux/Mac**: Seguir instrucciones en `DOCKER_SETUP.md`

### 3. Ejecutar la Aplicación

#### Opción A: Script Automático (Windows)
```bash
scripts\start.bat
```

### 4. Acceder a la Aplicación
- Abrir navegador en: http://localhost:4200
- Usuario: `admin`
- Contraseña: `admin`

## Tiempo de Primera Ejecución
- **Primera vez**: 5-10 minutos (descarga imágenes y construye contenedores)
- **Siguientes veces**: 1-2 minutos

## Comandos Útiles

### Ver logs en tiempo real
```bash
docker-compose logs -f
```

### Ver solo logs del backend
```bash
docker-compose logs -f backend
```

### Ver solo logs del frontend
```bash
docker-compose logs -f frontend
```

### Detener la aplicación
```bash
docker-compose down
```

### Reconstruir completamente (si metemos cambios)
```bash
docker-compose up --build --force-recreate
```

## Solución de Problemas

### Error: "Puerto en uso"
```bash
# Detener contenedores existentes
docker-compose down

# Verificar qué está usando el puerto
netstat -ano | findstr :8080
netstat -ano | findstr :4200
```

### Error para: "Docker no encontrado"
1. Verificar que Docker Desktop esté ejecutándose
2. Reiniciar terminal/PowerShell
3. Verificar: `docker --version`

### Error para: "No se puede conectar al daemon de Docker"
1. Abrir Docker Desktop
2. Esperar a que inicie completamente (ícono en la bandeja del sistema)
3. Intentar nuevamente

### La aplicación no carga -> Hacer los siguientes pasos:
1. Esperar 2-3 minutos después de ejecutar el comando
2. Verificar logs: `docker-compose logs`
3. Verificar que ambos servicios estén corriendo: `docker-compose ps`

## Verificación de Estado

### Verificar que los servicios estén corriendo
```bash
docker-compose ps
```

Deberías ver algo como:
```
NAME                    COMMAND                  SERVICE             STATUS
facturacion-backend-1   "java -jar target/fa…"   backend             running
facturacion-frontend-1  "/docker-entrypoint.…"   frontend            running
```

### Probar endpoints manualmente
```bash
# Probar backend
curl http://localhost:8080/api/test

# Probar login
curl -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin\"}" http://localhost:8080/api/auth/login
```

## Consejos:

1. **Primera ejecución**: Sé paciente, Docker necesita descargar las imágenes base
2. **Cambios en código**: Usar `docker-compose up --build` para reconstruir
3. **Limpiar espacio**: `docker system prune` para limpiar imágenes no usadas
4. **Desarrollo**: Los cambios en código requieren reconstruir los contenedores

## Si No Funciona:

1. Verificar que Docker Desktop esté instalado y ejecutándose
2. Verificar que los puertos 4200 y 8080 estén libres
3. Intentar con `docker-compose up --build --force-recreate`
4. Revisar logs completos con `docker-compose logs`
5. Contactame xD