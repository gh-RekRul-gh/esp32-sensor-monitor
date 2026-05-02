# Stage 1: build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY sensor-simulator/pom.xml sensor-simulator/pom.xml
RUN mvn dependency:go-offline -pl sensor-simulator -am -q
COPY sensor-simulator/src sensor-simulator/src
RUN mvn package -pl sensor-simulator -DskipTests -q

# Stage 2: run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/sensor-simulator/target/sensor-simulator-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
