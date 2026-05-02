# Stage 1: build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY sensor-monitor/pom.xml sensor-monitor/pom.xml
COPY sensor-monitor/.mvn sensor-monitor/.mvn
COPY sensor-monitor/mvnw sensor-monitor/mvnw
RUN chmod +x sensor-monitor/mvnw && \
    sensor-monitor/mvnw -f pom.xml dependency:go-offline -pl sensor-monitor -am -q

COPY sensor-monitor/src sensor-monitor/src
RUN sensor-monitor/mvnw -f pom.xml package -pl sensor-monitor -DskipTests -q

# Stage 2: run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/sensor-monitor/target/sensor-monitor-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
