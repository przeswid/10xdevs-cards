# Build stage
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

# Install tools needed for Maven wrapper to download Maven
RUN apk add --no-cache curl

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable and download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM amazoncorretto:21-alpine

WORKDIR /app

# Install wget for healthcheck
RUN apk add --no-cache wget

# Copy the built JAR from builder stage (specific to avoid copying wrong JAR)
COPY --from=builder /app/target/cards-*.jar app.jar

# Expose port
EXPOSE 8080

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application with container-aware settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]