# --- compilación ---
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# --- ejecución ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S altoque && adduser -S altoque -G altoque && mkdir -p /data/archivos && chown -R altoque:altoque /data
COPY --from=build /src/target/altoque-api.jar app.jar
USER altoque
ENV SPRING_PROFILES_ACTIVE=prod     ARCHIVOS_DIR=/data/archivos \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=America/Lima"
# XML y CDR: monta aquí un volumen persistente y respáldalo (SUNAT exige conservarlos).
VOLUME /data/archivos
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
