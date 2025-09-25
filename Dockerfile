# Multi-stage build
FROM gradle:7.4-jdk11 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
# Use the container's gradle instead of wrapper to avoid wrapper issues
RUN gradle build -x test

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# Configure volume for database persistence
VOLUME /app/data
# Set environment variables with defaults
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/app/data/dev.db
ENV JWT_SECRET=changeme
# Non-root user for security
RUN adduser --system --group appuser && \
    mkdir -p /app/data && \
    chown -R appuser:appuser /app
USER appuser
# Health check
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/tags || exit 1
# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
