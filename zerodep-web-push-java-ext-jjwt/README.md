# zerodep-web-push-java-ext-jjwt

Provides an implementation for `com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator`
utilizing [Java JWT](https://github.com/jwtk/jjwt).

## Requirements

The recommended version
of [io.jsonwebtoken:jjwt-api](https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api) and the
other runtime dependencies is 0.11.0 or higher(The latest version is more desirable).

## Usage

### pom.xml

You can use this sub-module by adding the dependency to your pom.xml.

Usually, in addition to this, you have to specify Java JWT's `runtime` dependencies(
see https://github.com/jwtk/jjwt#jdk-projects).

```

TBD

<!-- Java JWT's runtime dependencies. -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.2</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId> <!-- or jjwt-gson if Gson is preferred -->
    <version>0.11.2</version>
    <scope>runtime</scope>
</dependency>

```

### java

By calling `com.zerodeplibs.webpush.VAPIDKeyPairs#of(PrivateKeySource, PublicKeySource)`, the
implementation class provided by this sub-module is loaded automatically.

``` java

VAPIDKeyPairs.of(
    PrivateKeySources.of..... ,
    PublicKeySources.of.......
);

```
