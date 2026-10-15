@echo off
rem Abre Al Toque en este equipo: enciende Docker, levanta la base, la API y la web, y abre el navegador.
title Al Toque
cd /d "%~dp0"

docker info >nul 2>&1
if errorlevel 1 (
  echo Encendiendo Docker Desktop...
  start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
  for /l %%i in (1,1,60) do (
    timeout /t 3 /nobreak >nul
    docker info >nul 2>&1 && goto docker_listo
  )
  echo No se pudo encender Docker Desktop. Abrelo a mano y vuelve a ejecutar este archivo.
  pause
  exit /b 1
)
:docker_listo

if not exist .env (
  echo Falta el archivo .env. Copia .env.example como .env y cambia las claves.
  pause
  exit /b 1
)

echo Levantando Al Toque (la primera vez tarda unos minutos)...
docker compose up -d --build
if errorlevel 1 (
  pause
  exit /b 1
)

echo Esperando a que la API responda...
for /l %%i in (1,1,60) do (
  curl -s -o nul -f http://localhost:8081/api/planes && goto api_lista
  timeout /t 3 /nobreak >nul
)
echo La API no respondio. Revisa con: docker compose logs api
pause
exit /b 1
:api_lista

echo.
echo  Al Toque esta listo en http://localhost:8081
echo  Usuario demo:  demo@altoque.pe
echo  Contrasena:    altoque123
echo.
start "" http://localhost:8081
timeout /t 8 >nul
