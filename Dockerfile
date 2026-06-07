# Etapa 1: construcción del proyecto
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Etapa 2: ejecución de la aplicación
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Carpeta donde se montará EFS dentro del contenedor
RUN mkdir -p /app/efs

EXPOSE 8080

ENV EFS_MOUNT_PATH=/app/efs

ENTRYPOINT ["java", "-jar", "app.jar"]