# --- compilación ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# --- servidor ---
FROM nginx:1.27-alpine
# API_UPSTREAM: dirección interna del backend (en Dokploy, el nombre del servicio).
ENV API_UPSTREAM=http://api:8080
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/altoque-web/browser /usr/share/nginx/html
EXPOSE 80
