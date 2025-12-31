# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

zerodep-web-push-java is a Java library for implementing Web Push (RFC 8030) server-side functionality, with support for VAPID (RFC 8292) and Message Encryption (RFC 8291). The library is designed to be framework-agnostic and integrates with various third-party HTTP clients and JWT libraries.

**Key Design Principles:**
- Zero forced dependencies: Users choose their own JWT and HTTP client libraries
- Java Cryptography Architecture (JCA) for cryptographic operations
- Java 11+ (v2.x.x), with v1.x.x supporting Java 8+
- Multi-module Maven project following git-flow branching model

## Build Commands

### Basic Build and Test
```bash
./mvnw clean install          # Build all modules
./mvnw clean test             # Run tests for all modules
./mvnw test -pl :zerodep-web-push-java  # Test core module only
```

### Comprehensive Testing
```bash
sh ./scripts/test-core.sh     # Test core with multiple HTTP client versions
sh ./scripts/test-ext.sh      # Test all JWT extension modules
```

These scripts test against minimum supported versions and latest versions of dependencies (OkHttp, Apache HttpClient, Jetty 9/10/11, Vert.x 3/4, various JWT libraries).

### Code Quality
```bash
./mvnw checkstyle:check       # Run checkstyle (based on Google style)
```

Checkstyle runs automatically during the `validate` phase and will fail the build on violations.

### Running a Single Test
```bash
./mvnw test -Dtest=ClassName                    # Single test class
./mvnw test -Dtest=ClassName#methodName         # Single test method
./mvnw surefire:test -pl :zerodep-web-push-java -Dtest=ClassName  # Specific module
```

## Module Structure

### Core Module (`core/`)
**Artifact:** `com.zerodeplibs:zerodep-web-push-java`

Main packages:
- `com.zerodeplibs.webpush` - Core classes (VAPIDKeyPair, PushSubscription, MessageEncryption)
- `com.zerodeplibs.webpush.key` - Key management (PrivateKeySource, PublicKeySource, PEM parsing)
- `com.zerodeplibs.webpush.jwt` - JWT/VAPID (VAPIDJWTGenerator, DefaultVAPIDJWTGenerator)
- `com.zerodeplibs.webpush.httpclient` - HTTP client helpers (PreparerBuilder and implementations)
- `com.zerodeplibs.webpush.header` - Web Push headers (TTL, Urgency, Topic)

### Extension Modules (`ext-jwt/`)
Six JWT implementation sub-modules, each providing `VAPIDJWTGeneratorFactory`:
- `zerodep-web-push-java-ext-jwt-auth0` - Auth0 java-jwt
- `zerodep-web-push-java-ext-jwt-fusionauth` - FusionAuth JWT
- `zerodep-web-push-java-ext-jwt-jjwt` - JJWT
- `zerodep-web-push-java-ext-jwt-jose4j` - jose4j
- `zerodep-web-push-java-ext-jwt-nimbus-jose` - Nimbus JOSE + JWT
- `zerodep-web-push-java-ext-jwt-vertx` - Vert.x JWT Auth

### Examples (`examples/`)
- `basic/` - Spring Boot MVC example
- `webflux/` - Spring Boot WebFlux example
- `vertx/` - Vert.x standalone example

## Architecture Patterns

### Request Preparer Pattern
The library uses a builder pattern for creating HTTP requests:

1. **PreparerBuilder** - Abstract builder base class with fluent API
2. **Concrete Preparers** - Implementations for specific HTTP clients:
   - `StandardHttpClientRequestPreparer` - JDK 11+ HttpClient
   - `OkHttpClientRequestPreparer` - OkHttp 4.9.0+
   - `ApacheHttpClientRequestPreparer` - Apache HttpClient 5.1+
   - `JettyHttpClientRequestPreparer` - Jetty 9.4.33+, 10.0.0+, 11.0.0+
   - `VertxWebClientRequestPreparer` - Vert.x 3.9.2+, 4.0.0+

Each preparer handles:
- Extracting push service URL from PushSubscription
- Generating VAPID JWT (Authorization header)
- Encrypting push message payload
- Setting Web Push headers (TTL, Urgency, Topic)

### JWT Generation Strategy
The library supports multiple JWT generation approaches:

1. **Default (no dependencies):** `DefaultVAPIDJWTGenerator` - Built-in implementation using JCA
2. **Service Provider Interface:** `VAPIDJWTGeneratorFactory` loaded via ServiceLoader
3. **Direct instantiation:** Pass custom factory to `VAPIDKeyPairs.of()`

Extension modules register their factories in `module-info.java` using `provides`.

### Key Management
Keys can be loaded from multiple sources via factory methods:
- `PrivateKeySources.ofPEMFile()` / `PublicKeySources.ofPEMFile()` - PEM files
- `PrivateKeySources.ofDERFile()` / `PublicKeySources.ofDERFile()` - DER files
- `PrivateKeySources.ofBytes()` / `PublicKeySources.ofBytes()` - Byte arrays
- `PrivateKeySources.of()` / `PublicKeySources.of()` - Java Key objects

All keys must be ECDSA P-256 (secp256r1) curve.

## Testing Strategy

### Dual Test Execution
The core module runs tests twice (see `core/pom.xml`):
1. **default-test:** Without BouncyCastle provider (tests JDK crypto)
2. **test-with-bouncy-castle:** With BouncyCastle provider (tests alternative JCA provider)

### Multi-Version Testing
Scripts test against minimum and latest supported versions of optional dependencies to ensure compatibility ranges work correctly.

## Git Workflow

- **Production branch:** `main`
- **Development branch:** `dev`
- **Branch prefixes:** `feature-`, `release-`, `hotfix-`
- **Always open pull requests against `dev`**

The project uses `gitflow-maven-plugin` for release management.

## Release Process

Releases use the `release` Maven profile which:
- Generates source and javadoc JARs
- Signs artifacts with GPG
- Publishes to Maven Central via `central-publishing-maven-plugin`

Do not manually trigger releases without understanding the gitflow process.

## Code Style

- **Checkstyle:** Based on Google Java Style with custom modifications (see `checkstyle.xml`)
- **Line length:** Max 100 characters (`.editorconfig`)
- **Indentation:** 4 spaces, no tabs
- **Encoding:** UTF-8
- **Line endings:** LF

## Null Safety

The library has strict null safety:
- Public methods NEVER accept `null` arguments (throw exception)
- Public methods NEVER return `null` (use `Optional` when needed)
- **Exceptions:** `PushSubscription` class (JavaBean-style) and exception `getCause()`

## Common Development Patterns

### Adding a New HTTP Client Preparer
1. Extend `PreparerBuilder<YourPreparer>`
2. Implement `build(VAPIDKeyPair)` method
3. Create preparer class that handles library-specific request construction
4. Add as optional dependency in `core/pom.xml`
5. Update `module-info.java` with `requires static`
6. Write tests with multiple versions of the HTTP client library

### Adding a New JWT Extension Module
1. Create new module under `ext-jwt/`
2. Depend on `zerodep-web-push-java` core
3. Implement `VAPIDJWTGenerator` and `VAPIDJWTGeneratorFactory`
4. Register factory in `module-info.java` using `provides`
5. Add version compatibility tests in `scripts/test-ext.sh`
6. Ensure thread-safety (see ext-jwt README for requirements)

### Working with Cryptographic Operations
All crypto uses JCA APIs:
- `SecureRandom` - Random number generation
- `KeyFactory.getInstance("EC")` - Key conversion
- `KeyPairGenerator.getInstance("EC")` - Key generation (secp256r1)
- `Signature.getInstance("SHA256withECDSA")` - Signing
- `KeyAgreement.getInstance("ECDH")` - Key agreement
- `Mac.getInstance("HmacSHA256")` - HMAC
- `Cipher.getInstance("AES/GCM/NoPadding")` - Encryption

Default providers are `SunEC` and `SunJCE`, but any JCA provider works.
