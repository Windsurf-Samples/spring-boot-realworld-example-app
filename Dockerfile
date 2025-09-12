FROM openjdk:11-jdk-slim-buster as builder
WORKDIR /app
COPY gradle gradle
COPY build.gradle gradlew ./
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM alpine:3.18
RUN apk add --no-cache openjdk11-jre-headless wget
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
RUN mkdir -p /app/data && chown -R 1001:1001 /app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
USER 1001:1001
ENTRYPOINT ["java", "-Dspring.datasource.url=jdbc:sqlite:/app/data/dev.db", "-jar", "/app/app.jar"]
