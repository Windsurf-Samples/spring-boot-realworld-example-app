# ![RealWorld Example App using Kotlin and Spring](example-logo.png)

[![Actions](https://github.com/gothinkster/spring-boot-realworld-example-app/workflows/Java%20CI/badge.svg)](https://github.com/gothinkster/spring-boot-realworld-example-app/actions)

> ### Spring boot + MyBatis codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld-example-apps) spec and API.

This codebase was created to demonstrate a fully fledged full-stack application built with Spring boot + Mybatis including CRUD operations, authentication, routing, pagination, and more.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

# *NEW* GraphQL Support  

Following some DDD principles. REST or GraphQL is just a kind of adapter. And the domain layer will be consistent all the time. So this repository implement GraphQL and REST at the same time.

The GraphQL schema is https://github.com/gothinkster/spring-boot-realworld-example-app/blob/master/src/main/resources/schema/schema.graphqls and the visualization looks like below.

![](graphql-schema.png)

And this implementation is using [dgs-framework](https://github.com/Netflix/dgs-framework) which is a quite new java graphql server framework.
# How it works

The application uses Spring Boot (Web, Mybatis).

* Use the idea of Domain Driven Design to separate the business term and infrastructure term.
* Use MyBatis to implement the [Data Mapper](https://martinfowler.com/eaaCatalog/dataMapper.html) pattern for persistence.
* Use [CQRS](https://martinfowler.com/bliki/CQRS.html) pattern to separate the read model and write model.

And the code is organized as this:

1. `api` is the web layer implemented by Spring MVC
2. `core` is the business model including entities and services
3. `application` is the high-level services for querying the data transfer objects
4. `infrastructure`  contains all the implementation classes as the technique details

# Security

Integration with Spring Security and add other filter for jwt token process.

The secret key is stored in `application.properties`.

# Database

It uses a ~~H2 in-memory database~~ sqlite database (for easy local test without losing test data after every restart), can be changed easily in the `application.properties` for any other database.

# Getting started

You'll need Java 11 installed.

    ./gradlew bootRun

To test that it works, open a browser tab at http://localhost:8080/tags .  
Alternatively, you can run

    curl http://localhost:8080/tags

# Docker Containerization

This application has been containerized using Docker with a multi-stage build for optimal image size and security.

## Building the Docker Image

You'll need Docker installed.

```bash
docker build -t spring-boot-realworld .
```

## Running the Container

### Basic Usage
```bash
docker run -p 8080:8080 spring-boot-realworld
```

### With Database Persistence
```bash
# Create a data directory for database persistence
mkdir -p data
docker run -p 8080:8080 -v $(pwd)/data:/app/data spring-boot-realworld
```

### With Environment Variables
```bash
docker run -p 8080:8080 \
  -v $(pwd)/data:/app/data \
  -e SPRING_DATASOURCE_URL=jdbc:sqlite:/app/data/production.db \
  -e JWT_SECRET=your-secure-jwt-secret-here \
  spring-boot-realworld
```

## Environment Variables

The container supports the following environment variables:

- `SPRING_DATASOURCE_URL`: Database connection URL (default: `jdbc:sqlite:/app/data/dev.db`)
- `JWT_SECRET`: Secret key for JWT token signing (default: provided fallback value)

## Database Persistence

The container uses a Docker volume mounted at `/app/data` to persist the SQLite database. This ensures your data survives container restarts and updates.

### Database Backup
To backup your database:
```bash
# Copy database from running container
docker cp <container_id>:/app/data/dev.db ./backup-dev.db

# Or backup from volume mount
cp data/dev.db backup-dev.db
```

## Container Security

- The application runs as a non-root user (`appuser`) for enhanced security
- Health checks are configured to monitor application status
- Only necessary files are included in the image via `.dockerignore`

## Local Development with Containers

For local development, you can use the container while maintaining code changes:

```bash
# Build and run for development
docker build -t spring-boot-realworld-dev .
docker run -p 8080:8080 -v $(pwd)/data:/app/data spring-boot-realworld-dev
```

## Considerations for Production

### SQLite vs Other Databases
While SQLite works well for development and small deployments, consider these factors for production:

- **Pros**: Simple setup, no external dependencies, good for single-instance deployments
- **Cons**: Limited concurrent write performance, not suitable for multi-instance deployments
- **Alternatives**: For production at scale, consider PostgreSQL or MySQL with external database services

### Logging in Containers
Application logs are written to stdout/stderr and can be viewed with:
```bash
docker logs <container_id>
```

For production, consider using a logging driver or external log aggregation service.

# Try it out with Spring Boot's built-in Docker support

Alternatively, you can use Spring Boot's built-in Docker image building:
	
    ./gradlew bootBuildImage --imageName spring-boot-realworld-example-app
    docker run -p 8081:8080 spring-boot-realworld-example-app

# Try it out with a RealWorld frontend

The entry point address of the backend API is at http://localhost:8080, **not** http://localhost:8080/api as some of the frontend documentation suggests.

# Run test

The repository contains a lot of test cases to cover both api test and repository test.

    ./gradlew test

# Code format

Use spotless for code format.

    ./gradlew spotlessJavaApply

# Help

Please fork and PR to improve the project.
