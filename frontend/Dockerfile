# Etapa 1: Construcción
FROM node:18-alpine AS build

WORKDIR /app

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias
RUN npm ci

# Copiar código fuente
COPY . .

# Construir la aplicación
RUN npm run build

# Etapa 2: Servir con nginx
FROM nginx:alpine

# Copiar archivos construidos
COPY --from=build /app/dist/factullama-frontend /usr/share/nginx/html

# Copiar configuración personalizada de nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Exponer puerto 4200
EXPOSE 4200

CMD ["nginx", "-g", "daemon off;"]