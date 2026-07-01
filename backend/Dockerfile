# Usar imagen base de OpenJDK 17
FROM eclipse-temurin:21-jdk-alpine

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY src ./src

# Instalar Maven
RUN apk add --no-cache maven

# Compilar la aplicación (--no-transfer-progress reduce ruido en logs)
RUN mvn clean package -DskipTests --no-transfer-progress

# Exponer el puerto 8080
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/facturacion-0.0.1-SNAPSHOT.jar"]