FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/ProyectoHorario_1-0.0.1-SNAPSHOT.jar /app/ProyectoHorario_1.jar

EXPOSE ${PORT:-8080}

ENTRYPOINT ["java", "-jar", "/app/ProyectoHorario_1.jar"]