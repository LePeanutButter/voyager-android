# Multi-stage build for Kotlin/JVM application
# Stage 1: Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

# Install necessary build tools
RUN apk add --no-cache curl tar unzip

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and project files first for better Docker caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY app/build.gradle.kts app/
COPY app/src app/src

# Make gradlew executable
RUN chmod +x gradlew

# Cache Gradle dependencies
RUN ./gradlew dependencies --configuration-cache --no-daemon --no-scan --parallel

# Build the application
RUN ./gradlew assembleRelease --no-daemon --no-scan --parallel

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install necessary runtime packages
RUN apk add --no-cache tzdata curl

# Set timezone to UTC
ENV TZ=UTC

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -D -s /bin/sh -u 1001 -G appgroup appuser

# Set working directory
WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/app/build/outputs/apk/release/app-release.apk ./app.apk

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port (for web services if applicable)
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV APP_OPTS=""

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Default command to run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.apk $APP_OPTS"]

# Labels for metadata
LABEL maintainer="Voyager Team" \
      version="1.0.0" \
      description="SmarTrip - Kotlin-based Android App Architecture" \
      org.opencontainers.image.source="https://github.com/LePeanutButter/voyager-android" \
      org.opencontainers.image.licenses="GPL-3.0"
