# Etapa 1: construccion del proyecto
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Etapa 2: ejecucion de la aplicacion
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Carpeta donde se montara el almacenamiento local/EFS simulado
RUN mkdir -p /app/efs

EXPOSE 8080

ENV EFS_MOUNT_PATH=/app/efs

ENTRYPOINT ["java", "-jar", "app.jar"]