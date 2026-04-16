# Стадия 1: сборка
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -DskipTests -q

# Стадия 2: запуск
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/sensor-monitor-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
