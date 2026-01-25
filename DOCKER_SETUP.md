# Guía de Instalación de Docker

## Windows

### Opción 1: Docker Desktop (Recomendado)
1. Descargar Docker Desktop desde: https://www.docker.com/products/docker-desktop/
2. Ejecutar el instalador descargado
3. Seguir las instrucciones del instalador
4. Reiniciar el equipo cuando se solicite
5. Abrir Docker Desktop y esperar a que inicie completamente
6. Verificar instalación abriendo PowerShell/CMD y ejecutar:
   ```bash
   docker --version
   docker-compose --version
   ```

### Opción 2: Docker con WSL2 (Avanzado)
1. Habilitar WSL2 en Windows
2. Instalar una distribución de Linux (Ubuntu recomendado)
3. Instalar Docker Desktop con backend WSL2

## Linux (Ubuntu/Debian)

```bash
# Actualizar paquetes
sudo apt update

# Instalar dependencias
sudo apt install apt-transport-https ca-certificates curl software-properties-common

# Agregar clave GPG de Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Agregar repositorio de Docker
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Actualizar paquetes nuevamente
sudo apt update

# Instalar Docker
sudo apt install docker-ce docker-ce-cli containerd.io

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Agregar usuario al grupo docker (opcional, para no usar sudo)
sudo usermod -aG docker $USER

# Reiniciar sesión o ejecutar:
newgrp docker

# Verificar instalación
docker --version
docker-compose --version
```

## macOS

### Opción 1: Docker Desktop
1. Descargar Docker Desktop desde: https://www.docker.com/products/docker-desktop/
2. Arrastrar Docker.app a la carpeta Applications
3. Ejecutar Docker desde Applications
4. Seguir las instrucciones de configuración
5. Verificar instalación en Terminal:
   ```bash
   docker --version
   docker-compose --version
   ```

### Opción 2: Homebrew
```bash
# Instalar Homebrew si no lo tienes
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Instalar Docker
brew install --cask docker

# Ejecutar Docker Desktop desde Applications
```

## Verificación de Instalación

Después de instalar Docker, ejecutar estos comandos para verificar:

```bash
# Verificar versión de Docker
docker --version

# Verificar versión de Docker Compose
docker-compose --version

# Probar Docker con un contenedor de prueba
docker run hello-world
```

## Solución de Problemas Comunes

### Windows
- **Error: "Docker Desktop requires Windows 10 Pro/Enterprise"**
  - Solución: Usar Docker Toolbox o actualizar a Windows 10 Pro
  
- **Error: "WSL 2 installation is incomplete"**
  - Solución: Instalar WSL2 manualmente desde Microsoft Store

### Linux
- **Error: "Permission denied"**
  - Solución: Agregar usuario al grupo docker: `sudo usermod -aG docker $USER`
  
- **Error: "Docker daemon not running"**
  - Solución: `sudo systemctl start docker`

### macOS
- **Error: "Docker Desktop is not running"**
  - Solución: Abrir Docker Desktop desde Applications

## Configuración Recomendada

### Recursos (Docker Desktop)
- **CPU**: Mínimo 2 cores, recomendado 4+
- **RAM**: Mínimo 4GB, recomendado 8GB+
- **Disco**: Mínimo 20GB libres

### Configuración de Red
- Verificar que los puertos 4200 y 8080 estén disponibles
- Configurar firewall si es necesario