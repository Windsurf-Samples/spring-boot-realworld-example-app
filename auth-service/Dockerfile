FROM openjdk:11-jre-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
