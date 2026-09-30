FROM maven:3.9.11-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean verify
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/productos-reactive-ms-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
