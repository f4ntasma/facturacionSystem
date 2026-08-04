# Etapa 1: Construcción y control de versiones
FROM node:22-alpine AS build

WORKDIR /app

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias
RUN npm install --legacy-peer-deps

# Copiar código fuente
COPY . .

# Construir la aplicación
RUN npm run build

# Etapa 2: Servir con nginx
FROM nginx:alpine

# FIX 1: Añadir /browser a la ruta de Angular (Angular 17+)
COPY --from=build /app/dist/factullama-frontend/browser /usr/share/nginx/html

# FIX 2: Como tu archivo tiene el bloque 'http', debe reemplazar el global
COPY nginx.conf /etc/nginx/nginx.conf

# Exponer puerto 4200
EXPOSE 4200

CMD ["nginx", "-g", "daemon off;"]