# Migration Plan: Java 17 and Spring Boot 3 Upgrade

This document provides a detailed migration plan for upgrading the spring-boot-realworld-example-app from Java 11 and Spring Boot 2.6.3 to Java 17 and Spring Boot 3.

## Executive Summary

This migration involves upgrading from Java 11 to Java 17 and from Spring Boot 2.6.3 to Spring Boot 3.x. Spring Boot 3 is a major release that requires Java 17 as a minimum baseline and includes significant breaking changes, most notably the migration from Java EE (javax namespace) to Jakarta EE (jakarta namespace).

## Current State

| Component | Current Version |
|-----------|-----------------|
| Java | 11 |
| Spring Boot | 2.6.3 |
| Spring Dependency Management | 1.0.11.RELEASE |
| MyBatis Spring Boot Starter | 2.2.2 |
| GraphQL DGS Spring Boot Starter | 4.9.21 |
| Netflix DGS Codegen | 5.0.6 |
| Spotless | 6.2.1 |
| jjwt | 0.11.2 |
| SQLite JDBC | 3.36.0.3 |
| Flyway | (managed by Spring Boot) |
| Joda-Time | 2.10.13 |
| REST Assured | 4.5.1 |
| Lombok | (managed by Spring Boot) |

## Target State

| Component | Target Version |
|-----------|----------------|
| Java | 17 |
| Spring Boot | 3.2.0 (or latest stable 3.x) |
| Spring Dependency Management | 1.1.4 (or latest compatible) |
| MyBatis Spring Boot Starter | 3.0.3 |
| GraphQL DGS Spring Boot Starter | 7.6.0 (or latest compatible) |
| Netflix DGS Codegen | 6.0.0 (or latest compatible) |
| Spotless | 6.25.0 (or latest) |
| jjwt | 0.12.3 (or latest) |
| SQLite JDBC | 3.44.1.0 (or latest) |
| Flyway | (managed by Spring Boot 3) |
| Joda-Time | 2.12.5 (or latest) |
| REST Assured | 5.4.0 (or latest) |
| Lombok | (managed by Spring Boot) |

---

## Phase 1: Build Configuration Updates

### 1.1 build.gradle Changes

The build.gradle file requires the following modifications:

#### Plugin Version Updates (Lines 1-7)

**Current:**
```gradle
plugins {
    id 'org.springframework.boot' version '2.6.3'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    id "com.netflix.dgs.codegen" version "5.0.6"
    id "com.diffplug.spotless" version "6.2.1"
}
```

**Target:**
```gradle
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
    id "com.netflix.dgs.codegen" version "6.0.0"
    id "com.diffplug.spotless" version "6.25.0"
}
```

#### Java Compatibility Settings (Lines 10-11)

**Current:**
```gradle
sourceCompatibility = '11'
targetCompatibility = '11'
```

**Target:**
```gradle
sourceCompatibility = '17'
targetCompatibility = '17'
```

#### Dependency Version Updates (Lines 33-57)

**Current Dependencies Requiring Updates:**

| Dependency | Current Version | Target Version | Notes |
|------------|-----------------|----------------|-------|
| mybatis-spring-boot-starter | 2.2.2 | 3.0.3 | Required for Spring Boot 3 compatibility |
| graphql-dgs-spring-boot-starter | 4.9.21 | 7.6.0 | Required for Jakarta EE support |
| jjwt-api/impl/jackson | 0.11.2 | 0.12.3 | Recommended update |
| sqlite-jdbc | 3.36.0.3 | 3.44.1.0 | Recommended update |
| joda-time | 2.10.13 | 2.12.5 | Recommended update |
| rest-assured (all modules) | 4.5.1 | 5.4.0 | Required for Jakarta EE support |

**Target dependencies block:**
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-hateoas'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:7.6.0'
    implementation 'org.flywaydb:flyway-core'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3',
                'io.jsonwebtoken:jjwt-jackson:0.12.3'
    implementation 'joda-time:joda-time:2.12.5'
    implementation 'org.xerial:sqlite-jdbc:3.44.1.0'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'io.rest-assured:rest-assured:5.4.0'
    testImplementation 'io.rest-assured:json-path:5.4.0'
    testImplementation 'io.rest-assured:xml-path:5.4.0'
    testImplementation 'io.rest-assured:spring-mock-mvc:5.4.0'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3'
}
```

---

## Phase 2: CI/CD Pipeline Updates

### 2.1 .github/workflows/gradle.yml Changes

The GitHub Actions workflow file requires updates to use Java 17.

**Current (Lines 18-24):**
```yaml
steps:
- uses: actions/checkout@v2
- name: Set up JDK 11
  uses: actions/setup-java@v2
  with:
    distribution: zulu
    java-version: '11'
```

**Target:**
```yaml
steps:
- uses: actions/checkout@v4
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    distribution: zulu
    java-version: '17'
```

**Additional recommended updates:**
- Update `actions/checkout` from v2 to v4
- Update `actions/setup-java` from v2 to v4
- Update `actions/cache` from v2 to v4

---

## Phase 3: Jakarta EE Namespace Migration

Spring Boot 3 requires migrating from `javax.*` to `jakarta.*` namespaces. This is the most significant code change required.

### 3.1 Files Requiring javax to jakarta Migration

The following files contain `javax.*` imports that must be changed to `jakarta.*`:

#### Validation Imports (javax.validation -> jakarta.validation)

| File | Lines | Current Import | Target Import |
|------|-------|----------------|---------------|
| `src/main/java/io/spring/api/ArticleApi.java` | 15 | `javax.validation.Valid` | `jakarta.validation.Valid` |
| `src/main/java/io/spring/api/ArticlesApi.java` | 10 | `javax.validation.Valid` | `jakarta.validation.Valid` |
| `src/main/java/io/spring/api/CommentsApi.java` | 17-18 | `javax.validation.Valid`, `javax.validation.constraints.NotBlank` | `jakarta.validation.Valid`, `jakarta.validation.constraints.NotBlank` |
| `src/main/java/io/spring/api/CurrentUserApi.java` | 12 | `javax.validation.Valid` | `jakarta.validation.Valid` |
| `src/main/java/io/spring/api/UsersApi.java` | 18-20 | `javax.validation.Valid`, `javax.validation.constraints.Email`, `javax.validation.constraints.NotBlank` | `jakarta.validation.Valid`, `jakarta.validation.constraints.Email`, `jakarta.validation.constraints.NotBlank` |
| `src/main/java/io/spring/api/exception/CustomizeExceptionHandler.java` | 10-11 | `javax.validation.ConstraintViolation`, `javax.validation.ConstraintViolationException` | `jakarta.validation.ConstraintViolation`, `jakarta.validation.ConstraintViolationException` |
| `src/main/java/io/spring/application/article/ArticleCommandService.java` | 6 | `javax.validation.Valid` | `jakarta.validation.Valid` |
| `src/main/java/io/spring/application/article/DuplicatedArticleConstraint.java` | 8-9 | `javax.validation.Constraint`, `javax.validation.Payload` | `jakarta.validation.Constraint`, `jakarta.validation.Payload` |
| `src/main/java/io/spring/application/article/DuplicatedArticleValidator.java` | 5-6 | `javax.validation.ConstraintValidator`, `javax.validation.ConstraintValidatorContext` | `jakarta.validation.ConstraintValidator`, `jakarta.validation.ConstraintValidatorContext` |
| `src/main/java/io/spring/application/article/NewArticleParam.java` | 5 | `javax.validation.constraints.NotBlank` | `jakarta.validation.constraints.NotBlank` |
| `src/main/java/io/spring/application/user/DuplicatedEmailConstraint.java` | 5-6 | `javax.validation.Constraint`, `javax.validation.Payload` | `jakarta.validation.Constraint`, `jakarta.validation.Payload` |
| `src/main/java/io/spring/application/user/DuplicatedEmailValidator.java` | 4-5 | `javax.validation.ConstraintValidator`, `javax.validation.ConstraintValidatorContext` | `jakarta.validation.ConstraintValidator`, `jakarta.validation.ConstraintValidatorContext` |
| `src/main/java/io/spring/application/user/DuplicatedUsernameConstraint.java` | 5-6 | `javax.validation.Constraint`, `javax.validation.Payload` | `jakarta.validation.Constraint`, `jakarta.validation.Payload` |
| `src/main/java/io/spring/application/user/DuplicatedUsernameValidator.java` | 4-5 | `javax.validation.ConstraintValidator`, `javax.validation.ConstraintValidatorContext` | `jakarta.validation.ConstraintValidator`, `jakarta.validation.ConstraintValidatorContext` |
| `src/main/java/io/spring/application/user/RegisterParam.java` | 4-5 | `javax.validation.constraints.Email`, `javax.validation.constraints.NotBlank` | `jakarta.validation.constraints.Email`, `jakarta.validation.constraints.NotBlank` |
| `src/main/java/io/spring/application/user/UpdateUserParam.java` | 4 | `javax.validation.constraints.Email` | `jakarta.validation.constraints.Email` |
| `src/main/java/io/spring/application/user/UserService.java` | 7-10 | `javax.validation.Constraint`, `javax.validation.ConstraintValidator`, `javax.validation.ConstraintValidatorContext`, `javax.validation.Valid` | `jakarta.validation.Constraint`, `jakarta.validation.ConstraintValidator`, `jakarta.validation.ConstraintValidatorContext`, `jakarta.validation.Valid` |
| `src/main/java/io/spring/graphql/UserMutation.java` | 21 | `javax.validation.ConstraintViolationException` | `jakarta.validation.ConstraintViolationException` |
| `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java` | 20-21 | `javax.validation.ConstraintViolation`, `javax.validation.ConstraintViolationException` | `jakarta.validation.ConstraintViolation`, `jakarta.validation.ConstraintViolationException` |

#### Servlet Imports (javax.servlet -> jakarta.servlet)

| File | Lines | Current Import | Target Import |
|------|-------|----------------|---------------|
| `src/main/java/io/spring/api/security/JwtTokenFilter.java` | 8-11 | `javax.servlet.FilterChain`, `javax.servlet.ServletException`, `javax.servlet.http.HttpServletRequest`, `javax.servlet.http.HttpServletResponse` | `jakarta.servlet.FilterChain`, `jakarta.servlet.ServletException`, `jakarta.servlet.http.HttpServletRequest`, `jakarta.servlet.http.HttpServletResponse` |

#### Crypto Imports (No Change Required)

The following file uses `javax.crypto` which is part of the JDK and does NOT need to be changed:

| File | Lines | Import | Action |
|------|-------|--------|--------|
| `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java` | 11-12 | `javax.crypto.SecretKey`, `javax.crypto.spec.SecretKeySpec` | **NO CHANGE** - Part of JDK, not Jakarta EE |

---

## Phase 4: Spring Security Configuration Updates

Spring Boot 3 introduces significant changes to Spring Security configuration. The `WebSecurityConfigurerAdapter` class has been removed.

### 4.1 WebSecurityConfig.java Refactoring

**Current approach (deprecated):**
```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .cors()
            .and()
            // ... configuration
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            // ...
    }
}
```

**Target approach (Spring Security 6.x / Spring Boot 3.x):**
```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/graphiql").permitAll()
                .requestMatchers("/graphql").permitAll()
                .requestMatchers(HttpMethod.GET, "/articles/feed").authenticated()
                .requestMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/articles/**", "/profiles/**", "/tags").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // ... same as before
    }
}
```

**Key changes:**
1. Remove `extends WebSecurityConfigurerAdapter`
2. Replace `configure(HttpSecurity http)` with `@Bean SecurityFilterChain filterChain(HttpSecurity http)`
3. Replace `authorizeRequests()` with `authorizeHttpRequests()`
4. Replace `antMatchers()` with `requestMatchers()`
5. Use lambda-based configuration for `csrf()`, `cors()`, `exceptionHandling()`, `sessionManagement()`
6. Return `http.build()` from the method

---

## Phase 5: Documentation Updates

### 5.1 README.md Changes

**Current (Line 47):**
```markdown
You'll need Java 11 installed.
```

**Target:**
```markdown
You'll need Java 17 installed.
```

---

## Phase 6: Application Properties Updates

### 6.1 application.properties Review

Spring Boot 3 has deprecated or renamed several properties. Review and update as needed:

**Potential changes:**
- No immediate changes required for current properties
- Consider adding `spring.jpa.open-in-view=false` if JPA is added later
- Review any deprecated property warnings during startup

---

## Migration Sequence

Execute the migration in the following order to minimize issues:

### Step 1: Preparation
1. Create a new branch for the migration
2. Ensure all tests pass on the current version
3. Review and document any custom configurations

### Step 2: Build Configuration (build.gradle)
1. Update Java compatibility to 17
2. Update Spring Boot plugin version to 3.2.0
3. Update Spring Dependency Management plugin version to 1.1.4
4. Update Netflix DGS Codegen plugin version to 6.0.0
5. Update Spotless plugin version to 6.25.0
6. Update all dependency versions as documented above

### Step 3: Jakarta EE Namespace Migration
1. Run a global find-and-replace for `javax.validation` -> `jakarta.validation`
2. Run a global find-and-replace for `javax.servlet` -> `jakarta.servlet`
3. **DO NOT** change `javax.crypto` imports (these are JDK classes)
4. Verify all imports are correct

### Step 4: Spring Security Refactoring
1. Refactor `WebSecurityConfig.java` to use the new component-based approach
2. Remove `WebSecurityConfigurerAdapter` extension
3. Update method signatures and return types
4. Update authorization configuration syntax

### Step 5: CI/CD Updates
1. Update `.github/workflows/gradle.yml` to use Java 17
2. Update GitHub Actions versions (checkout, setup-java, cache)

### Step 6: Documentation Updates
1. Update README.md Java version requirement

### Step 7: Testing and Validation
1. Run `./gradlew clean build` to verify compilation
2. Run `./gradlew test` to verify all tests pass
3. Run `./gradlew bootRun` to verify application starts
4. Test key API endpoints manually
5. Test GraphQL endpoints

### Step 8: Code Formatting
1. Run `./gradlew spotlessApply` to ensure code formatting is consistent

---

## Potential Issues and Mitigations

### Issue 1: GraphQL DGS Compatibility
**Risk:** DGS framework version compatibility with Spring Boot 3
**Mitigation:** Use DGS version 7.x which has full Spring Boot 3 support. Check the [DGS release notes](https://github.com/Netflix/dgs-framework/releases) for any breaking changes.

### Issue 2: MyBatis Compatibility
**Risk:** MyBatis mapper configurations may need updates
**Mitigation:** Use MyBatis Spring Boot Starter 3.0.x which is designed for Spring Boot 3. Review mapper XML files for any deprecated features.

### Issue 3: Test Framework Changes
**Risk:** REST Assured and Spring Test may have API changes
**Mitigation:** Update to REST Assured 5.x and review test code for deprecated methods.

### Issue 4: Property Deprecations
**Risk:** Some application properties may be deprecated or renamed
**Mitigation:** Review Spring Boot 3 migration guide for property changes. Check startup logs for deprecation warnings.

### Issue 5: Lombok Compatibility
**Risk:** Lombok version compatibility with Java 17
**Mitigation:** Lombok is managed by Spring Boot and should be compatible. If issues arise, explicitly set a compatible Lombok version.

---

## Rollback Plan

If critical issues are discovered during migration:

1. Revert to the previous branch/commit
2. Document the specific issue encountered
3. Research the issue and update this migration plan
4. Attempt migration again with the updated plan

---

## Post-Migration Checklist

- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Application starts without errors
- [ ] REST API endpoints work correctly
- [ ] GraphQL endpoints work correctly
- [ ] Authentication/JWT functionality works
- [ ] Database operations work correctly
- [ ] CI/CD pipeline passes
- [ ] Code formatting is consistent
- [ ] No deprecation warnings in logs (or documented exceptions)

---

## References

- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Security 6.0 Migration Guide](https://docs.spring.io/spring-security/reference/migration/index.html)
- [Jakarta EE 9/10 Migration](https://jakarta.ee/resources/jakarta-ee-9-migration-guide/)
- [Netflix DGS Framework](https://netflix.github.io/dgs/)
- [MyBatis Spring Boot Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)

---

## Document Information

| Field | Value |
|-------|-------|
| Created | January 2026 |
| Author | Devin AI |
| Repository | spring-boot-realworld-example-app |
| Target Completion | TBD |
