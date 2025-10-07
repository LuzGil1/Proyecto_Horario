FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia solo el pom.xml primero
COPY pom.xml ./

# Descarga dependencias (esto se cachea)
RUN mvn dependency:go-offline -B

# Copia el código fuente
COPY src ./src

# Compila sin descargar dependencias de nuevo
RUN mvn package -DskipTests -B -o

# Verifica que el JAR se creó
RUN ls -la target/

# ===== SEGUNDA ETAPA =====
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia el JAR desde la etapa de build
COPY --from=build /app/target/ProyectoHorario_1-0.0.1-SNAPSHOT.jar /app/app.jar

# Expone el puerto (Railway usará la variable PORT)
EXPOSE 8080

# Ejecuta la aplicación
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "/app/app.jar"]