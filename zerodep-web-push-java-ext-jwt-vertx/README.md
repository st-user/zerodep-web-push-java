# zerodep-web-push-java-ext-jwt-vertx

Provides an implementation for `com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator`
utilizing [JWT Auth - Vert.x](https://vertx.io/docs/vertx-auth-jwt/java/).

## Requirements

The supported versions
of [io.vertx:vertx-auth-jwt](https://mvnrepository.com/artifact/io.vertx/vertx-auth-jwt) are the
following.

- Vert.x 3: 3.9.2 or higher.
- Vert.x 4: 4.0.0 or higher.

In both cases, the latest version is recommended.

## Usage

### pom.xml

You can use this sub-module by adding the dependency to your pom.xml.

If you want to use additional claims(`VAPIDJWTParam.getBuilder#additionalClaim`), you have to have
Jackson Databind on your classpath.

``` xml
<dependency>
    <groupId>com.zerodeplibs</groupId>
    <artifactId>zerodep-web-push-java</artifactId>
    <version>2.0.2</version>
</dependency>
<dependency>
    <groupId>com.zerodeplibs</groupId>
    <artifactId>zerodep-web-push-java-ext-jwt-vertx</artifactId>
    <version>2.0.2</version>
</dependency>

<!-- 
    For version consistency,
    the dependency on vertx-auth-jwt must be explicitly specified. 
-->
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-auth-jwt</artifactId>
    <version>${your.vertx.version}</version>
</dependency>

<!-- If you want to use additional claims -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>${you.jackson.version}</version>
</dependency>

```

### java

You have to create an instance of `com.zerodeplibs.webpush.jwt.VAPIDJWTGeneratorFactory` manually
and pass it
to `com.zerodeplibs.webpush.VAPIDKeyPairs#of(PrivateKeySource, PublicKeySource, BiFunction)`
.

For example:

``` java

Vertx vertx = Vertx.vertx();

VAPIDKeyPairs.of(
    PrivateKeySources.of..... ,
    PublicKeySources.of.......,
    new VertxVAPIDJWTGeneratorFactory(() -> vertx)
);

```

## MISC

### Thread-safety

The implementations of this sub-module are thread-safe when a given `vertxObtainStrategy` is
thread-safe.
