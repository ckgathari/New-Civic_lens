# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache Maven dependencies before copying source
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src src
RUN mvn clean package -DskipTests -q

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Run as non-root for security
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copy built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Render assigns a dynamic $PORT env var; default to 8080 locally
EXPOSE 8080

# Spring Boot respects --server.port; $PORT is injected by Render at runtime
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080}"]
