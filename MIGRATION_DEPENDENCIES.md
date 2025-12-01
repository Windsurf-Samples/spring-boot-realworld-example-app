# Spring Boot 3 Migration - Dependency Compatibility Analysis

This document provides a comprehensive analysis of all dependencies in the project and their compatibility with Spring Boot 3.x. This analysis was performed as part of Phase 1 of the Java 17 and Spring Boot 3 migration.

## Pre-Migration Test Results (Java 11 / Spring Boot 2.6.3)

**Test Date:** December 1, 2025  
**Java Version:** 11  
**Spring Boot Version:** 2.6.3  
**Total Tests:** 68  
**Tests Passed:** 68  
**Tests Failed:** 0  
**Build Status:** SUCCESS

All existing tests pass on the current Java 11 / Spring Boot 2.6.3 configuration.

## Post-Migration Test Results (Java 17 / Spring Boot 2.6.3)

**Test Date:** December 1, 2025  
**Java Version:** 17  
**Spring Boot Version:** 2.6.3  
**Total Tests:** 68  
**Tests Passed:** 68  
**Tests Failed:** 0  
**Build Status:** SUCCESS

All tests pass on Java 17 with Spring Boot 2.6.3 after the migration changes.

### Java 17 Compatibility Issues Resolved

#### Spotless Plugin (google-java-format)
| Attribute | Value |
|-----------|-------|
| **Issue** | The Spotless plugin version 6.2.1 uses an older version of google-java-format that doesn't support Java 17's module system |
| **Error** | `IllegalAccessError: class com.google.googlejavaformat.java.JavaInput cannot access class com.sun.tools.javac.parser.Tokens$TokenKind` |
| **Resolution** | Updated Spotless plugin from 6.2.1 to 6.25.0 |
| **Impact** | Build tooling only - no runtime impact |

---

## Dependency Compatibility Matrix

### Core Spring Boot Dependencies

| Dependency | Current Version | Required Version (Spring Boot 3) | Status | Notes |
|------------|-----------------|----------------------------------|--------|-------|
| Spring Boot | 2.6.3 | 3.0.0+ | Upgrade Required | Major version upgrade with breaking changes |
| Spring Dependency Management | 1.0.11.RELEASE | 1.1.0+ | Upgrade Required | Compatible with Spring Boot 3 |

### Database & ORM Dependencies

#### MyBatis Spring Boot Starter
| Attribute | Value |
|-----------|-------|
| **Current Version** | 2.2.2 |
| **Required Version** | 3.0.0+ |
| **Compatibility Status** | Upgrade Required |
| **Breaking Changes** | - Namespace migration from `javax.*` to `jakarta.*` - Minimum Java version requirement changed to Java 17 - Configuration property prefix changes |
| **Migration Notes** | MyBatis Spring Boot Starter 3.0.0+ is specifically designed for Spring Boot 3.x and includes Jakarta EE 9+ namespace support. The XML mapper files should continue to work without changes. |

#### SQLite JDBC
| Attribute | Value |
|-----------|-------|
| **Current Version** | 3.36.0.3 |
| **Required Version** | 3.42.0.0+ (recommended: 3.45.0.0+) |
| **Compatibility Status** | Upgrade Recommended |
| **Breaking Changes** | None significant |
| **Migration Notes** | SQLite JDBC is a pure JDBC driver and has no direct Spring Boot dependencies. Upgrading to the latest version is recommended for bug fixes and performance improvements. Version 3.45.0.0+ includes Java 17 optimizations. |

#### Flyway Core
| Attribute | Value |
|-----------|-------|
| **Current Version** | Managed by Spring Boot BOM |
| **Required Version** | 9.0.0+ (managed by Spring Boot 3 BOM) |
| **Compatibility Status** | Auto-managed |
| **Breaking Changes** | - Flyway 9.x requires Java 17 minimum - Some deprecated APIs removed |
| **Migration Notes** | Flyway version is managed by Spring Boot's dependency management. Spring Boot 3.x will automatically use a compatible Flyway version. |

### GraphQL Dependencies

#### Netflix DGS (Domain Graph Service)
| Attribute | Value |
|-----------|-------|
| **Current Version** | 4.9.21 |
| **Required Version** | 5.5.0+ (recommended: 7.x or 8.x for latest features) |
| **Compatibility Status** | Major Upgrade Required |
| **Breaking Changes** | - DGS 5.x+ requires Spring Boot 3.x - Jakarta EE namespace migration (`javax.*` to `jakarta.*`) - GraphQL Java version upgrade - Some deprecated APIs removed - DataFetcher signature changes in some cases |
| **Migration Notes** | Netflix DGS 5.x was the first version to support Spring Boot 3. Version 7.x and 8.x provide better Spring Boot 3.2+ compatibility. The GraphQL schema files remain unchanged. Custom exception handlers may need updates for new GraphQL Java APIs. |

#### DGS CodeGen Plugin
| Attribute | Value |
|-----------|-------|
| **Current Version** | 5.0.6 |
| **Required Version** | 5.6.0+ (recommended: 6.x+) |
| **Compatibility Status** | Upgrade Required |
| **Breaking Changes** | - Generated code uses Jakarta namespace - Package structure changes possible |
| **Migration Notes** | The codegen plugin should be upgraded alongside the DGS runtime. Generated types will use Jakarta EE namespaces. |

### Security Dependencies

#### JJWT (Java JWT)
| Attribute | Value |
|-----------|-------|
| **Current Version** | 0.11.2 |
| **Required Version** | 0.11.5+ (recommended: 0.12.x) |
| **Compatibility Status** | Compatible / Upgrade Recommended |
| **Breaking Changes** | - 0.12.x has API changes for parser creation - `parserBuilder()` method changes |
| **Migration Notes** | JJWT 0.11.x is compatible with both Java 11 and Java 17. Version 0.12.x includes security improvements and better Java 17 support. The current version (0.11.2) will work with Spring Boot 3, but upgrading to 0.12.x is recommended for security patches. If upgrading to 0.12.x, parser creation code needs updates: `Jwts.parserBuilder()` becomes `Jwts.parser()`. |

#### Spring Security
| Attribute | Value |
|-----------|-------|
| **Current Version** | Managed by Spring Boot BOM |
| **Required Version** | 6.0.0+ (managed by Spring Boot 3 BOM) |
| **Compatibility Status** | Auto-managed |
| **Breaking Changes** | - `WebSecurityConfigurerAdapter` removed (already deprecated) - Lambda DSL is now the standard - `authorizeRequests()` replaced with `authorizeHttpRequests()` - CSRF configuration changes |
| **Migration Notes** | Spring Security 6.x comes with Spring Boot 3.x. The project's `WebSecurityConfig.java` will need updates to use the new configuration style. |

### Utility Dependencies

#### Joda-Time
| Attribute | Value |
|-----------|-------|
| **Current Version** | 2.10.13 |
| **Required Version** | 2.12.0+ (or consider migration to java.time) |
| **Compatibility Status** | Compatible |
| **Breaking Changes** | None |
| **Migration Notes** | Joda-Time is fully compatible with Java 17 and Spring Boot 3. However, consider migrating to `java.time` (JSR-310) as Joda-Time is in maintenance mode. The project uses Joda-Time for `DateTime` in cursor pagination. |

#### Lombok
| Attribute | Value |
|-----------|-------|
| **Current Version** | Managed by Spring Boot BOM |
| **Required Version** | 1.18.26+ |
| **Compatibility Status** | Compatible |
| **Breaking Changes** | None significant for Java 17 |
| **Migration Notes** | Lombok works with Java 17. Ensure the version is 1.18.26+ for full Java 17 support. Spring Boot 3.x BOM manages a compatible version. |

### Testing Dependencies

#### REST Assured
| Attribute | Value |
|-----------|-------|
| **Current Version** | 4.5.1 |
| **Required Version** | 5.3.0+ |
| **Compatibility Status** | Upgrade Required |
| **Breaking Changes** | - Jakarta EE namespace migration - Some API changes |
| **Migration Notes** | REST Assured 5.x supports Jakarta EE namespaces required by Spring Boot 3. The `spring-mock-mvc` module needs to be updated to work with Spring 6.x. |

#### MyBatis Spring Boot Starter Test
| Attribute | Value |
|-----------|-------|
| **Current Version** | 2.2.2 |
| **Required Version** | 3.0.0+ |
| **Compatibility Status** | Upgrade Required |
| **Breaking Changes** | Same as MyBatis Spring Boot Starter |
| **Migration Notes** | Must be upgraded alongside the main MyBatis starter. |

---

## Summary of Required Changes for Spring Boot 3

### Phase 2 (Current Phase - Java 17 Only)
No dependency version changes required. Java 17 is compatible with all current dependency versions.

### Phase 3 (Future - Spring Boot 3 Upgrade)
The following dependencies will need version updates:

1. **Spring Boot**: 2.6.3 -> 3.2.x (or latest 3.x)
2. **MyBatis Spring Boot Starter**: 2.2.2 -> 3.0.3+
3. **Netflix DGS**: 4.9.21 -> 7.x or 8.x
4. **DGS CodeGen Plugin**: 5.0.6 -> 6.x
5. **REST Assured**: 4.5.1 -> 5.4.0+
6. **JJWT**: 0.11.2 -> 0.12.x (recommended)
7. **SQLite JDBC**: 3.36.0.3 -> 3.45.0.0+ (recommended)

### Code Changes Required for Spring Boot 3 (Phase 3)
1. **Namespace Migration**: All `javax.*` imports must change to `jakarta.*`
2. **Spring Security Configuration**: Update `WebSecurityConfig.java` to use new DSL
3. **GraphQL Exception Handler**: Update for new DGS/GraphQL Java APIs

---

## Risk Assessment

| Risk Level | Description |
|------------|-------------|
| **Low** | Java 17 upgrade (Phase 2) - All dependencies are compatible |
| **Medium** | Spring Boot 3 upgrade (Phase 3) - Requires careful dependency coordination |
| **High** | Netflix DGS upgrade - Most significant change due to major version jump |

---

## Recommendations

1. **Phase 2 (Java 17)**: Proceed with Java 17 upgrade without changing dependency versions. All current dependencies support Java 17.

2. **Phase 3 (Spring Boot 3)**: 
   - Upgrade all dependencies simultaneously to avoid version conflicts
   - Test thoroughly after each major dependency upgrade
   - Consider creating feature branches for testing individual dependency upgrades

3. **Future Consideration**: 
   - Evaluate migrating from Joda-Time to `java.time` API
   - Consider upgrading JJWT to 0.12.x for security improvements
