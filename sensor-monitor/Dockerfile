# Stage 1: build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
COPY pom.xml /app/pom.xml
WORKDIR /app/sensor-monitor
COPY sensor-monitor/pom.xml pom.xml
RUN mvn dependency:go-offline -q
COPY sensor-monitor/src src
RUN mvn package -DskipTests -q

# Stage 2: run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/sensor-monitor/target/sensor-monitor-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
