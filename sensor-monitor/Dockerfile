# Stage 1: build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY sensor-monitor/pom.xml sensor-monitor/pom.xml
RUN mvn dependency:go-offline -pl sensor-monitor -am -q
COPY sensor-monitor/src sensor-monitor/src
RUN mvn package -pl sensor-monitor -DskipTests -q

# Stage 2: run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/sensor-monitor/target/sensor-monitor-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
