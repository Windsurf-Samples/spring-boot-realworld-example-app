# ![RealWorld Example App using Java and Spring](example-logo.png)

[![Actions](https://github.com/gothinkster/spring-boot-realworld-example-app/workflows/Java%20CI/badge.svg)](https://github.com/gothinkster/spring-boot-realworld-example-app/actions)

> ### Spring Boot 2.6.3 + MyBatis codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld-example-apps) spec and API.

This codebase was created to demonstrate a fully fledged full-stack application built with Spring Boot 2.6.3 + MyBatis including CRUD operations, authentication, routing, pagination, and more. It provides both REST and GraphQL APIs.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

# GraphQL Support  

Following DDD principles, REST and GraphQL are just different kinds of adapters. The domain layer remains consistent across both. This repository implements GraphQL and REST APIs simultaneously using Netflix DGS framework 4.9.21.

**GraphQL Features:**
- **Queries:** article, articles, me, feed, profile, tags
- **Mutations:** createUser, login, updateUser, followUser, unfollowUser, createArticle, updateArticle, favoriteArticle, unfavoriteArticle, deleteArticle, addComment, deleteComment
- **Cursor-based pagination** for articles and comments using Connection types
- **GraphiQL interface** available at http://localhost:8080/graphiql for interactive exploration

The GraphQL schema is located at `src/main/resources/schema/schema.graphqls` and the visualization looks like below.

![](graphql-schema.png)

This implementation uses [Netflix DGS Framework](https://github.com/Netflix/dgs-framework) which is a modern Java GraphQL server framework.
# How it works

The application uses Spring Boot 2.6.3 with the following key technologies:

**Core Technologies:**
- **Spring Boot 2.6.3** - Application framework
- **MyBatis 2.2.2** - Persistence framework implementing the [Data Mapper](https://martinfowler.com/eaaCatalog/dataMapper.html) pattern
- **Netflix DGS 4.9.21** - GraphQL server framework
- **SQLite 3.36.0.3** - Embedded database
- **Flyway** - Database migration management
- **Lombok** - Reduces boilerplate code
- **Joda-Time 2.10.13** - Date/time handling

**Testing:**
- **REST Assured 4.5.1** - REST API testing
- **Spring Boot Test** - Integration testing support

**Architecture Patterns:**
- **Domain Driven Design (DDD)** - Separates business terms from infrastructure concerns
- **CQRS** - [Command Query Responsibility Segregation](https://martinfowler.com/bliki/CQRS.html) pattern separates read and write models
- **Data Mapper Pattern** - Clean separation between domain model and database

**Code Organization:**

1. `api` - Web layer with REST controllers and GraphQL resolvers (Spring MVC)
2. `core` - Domain layer with business entities and repository interfaces
3. `application` - Application services for queries and commands (DTOs, query services)
4. `infrastructure` - Infrastructure layer with MyBatis implementations and technical details
5. `graphql` - GraphQL-specific implementations (mutations, data fetchers)

# REST API Endpoints

The application provides a complete REST API following the RealWorld specification:

**User Management:**
- `POST /users` - User registration
- `POST /users/login` - User authentication
- `GET /user` - Get current user
- `PUT /user` - Update current user profile

**Articles:**
- `GET /articles` - List articles (supports filtering by tag, author, favorited)
- `POST /articles` - Create a new article (requires authentication)
- `GET /articles/feed` - Get personalized article feed (requires authentication)
- `GET /articles/{slug}` - Get a single article by slug
- `PUT /articles/{slug}` - Update an article (requires authentication and ownership)
- `DELETE /articles/{slug}` - Delete an article (requires authentication and ownership)

**Favorites:**
- `POST /articles/{slug}/favorite` - Favorite an article (requires authentication)
- `DELETE /articles/{slug}/favorite` - Unfavorite an article (requires authentication)

**Comments:**
- `GET /articles/{slug}/comments` - Get comments for an article
- `POST /articles/{slug}/comments` - Add a comment to an article (requires authentication)
- `DELETE /articles/{slug}/comments/{id}` - Delete a comment (requires authentication and ownership)

**Profiles:**
- `GET /profiles/{username}` - Get a user profile
- `POST /profiles/{username}/follow` - Follow a user (requires authentication)
- `DELETE /profiles/{username}/follow` - Unfollow a user (requires authentication)

**Tags:**
- `GET /tags` - Get all tags

# Security

The application uses Spring Security with JWT-based authentication:

**Authentication & Authorization:**
- **JWT Tokens** - Using JJWT 0.11.2 with HS512 algorithm
- **BCrypt Password Encoding** - Secure password hashing
- **Stateless Sessions** - No server-side session storage
- **Custom JWT Filter** - Extracts and validates JWT tokens from Authorization header

**Security Configuration:**
- CORS enabled with configurable origins
- Public endpoints: POST /users, POST /users/login, GET /articles/**, GET /profiles/**, GET /tags, /graphql, /graphiql
- Protected endpoints: Require valid JWT token in Authorization header
- Authorization checks for article and comment ownership

The JWT secret key is stored in `application.properties` (should be externalized in production).

# Database

The application uses **SQLite** as the database (for easy local testing without losing data after restarts). The database can be easily changed in `application.properties` for any other database.

**Database Schema:**
- `users` - User accounts and profiles
- `articles` - Blog articles with slug, title, description, and body
- `comments` - Comments on articles
- `tags` - Article tags for categorization
- `article_tags` - Many-to-many relationship between articles and tags
- `article_favorites` - Many-to-many relationship for user favorites
- `follows` - User follow relationships

**Database Management:**
- **Flyway** - Handles database migrations automatically on startup
- Migration files located in `src/main/resources/db/migration/`
- Initial schema: `V1__create_tables.sql`
- Database file: `dev.db` (SQLite file in project root)

# Getting started

You'll need Java 11 installed.

    ./gradlew bootRun

To test that it works, open a browser tab at http://localhost:8080/tags .  
Alternatively, you can run

    curl http://localhost:8080/tags

# Try it out with [Docker](https://www.docker.com/)

You'll need Docker installed.
	
    ./gradlew bootBuildImage --imageName spring-boot-realworld-example-app
    docker run -p 8081:8080 spring-boot-realworld-example-app

# Try it out with a RealWorld frontend

The entry point address of the backend API is at http://localhost:8080, **not** http://localhost:8080/api as some of the frontend documentation suggests.

# Run test

The repository contains a lot of test cases to cover both api test and repository test.

    ./gradlew test

# Code format

The project uses **Spotless 6.2.1** with **Google Java Format** for consistent code formatting.

**Format code:**

    ./gradlew spotlessJavaApply

**Check formatting:**

    ./gradlew spotlessJavaCheck

# Help

Please fork and PR to improve the project.
