# zerodep-web-push-java-ext-jwt-jose4j

Provides an implementation for `com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator`
utilizing [jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home).

## Requirements

The recommended version
of [org.bitbucket.b_c:jose4j](https://mvnrepository.com/artifact/org.bitbucket.b_c/jose4j) and the
other runtime dependencies is 0.7.0 or higher(The latest version is more desirable).

## Usage

### pom.xml

You can use this sub-module by adding the dependency to your pom.xml.

``` xml
<dependency>
    <groupId>com.zerodeplibs</groupId>
    <artifactId>zerodep-web-push-java</artifactId>
    <version>2.0.2</version>
</dependency>
<dependency>
    <groupId>com.zerodeplibs</groupId>
    <artifactId>zerodep-web-push-java-ext-jwt-jose4j</artifactId>
    <version>2.0.2</version>
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

## Limitations

When creating a JWT by calling `VAPIDJWTGenerator#generate`, the claims in the token's payload are
specified by the given `com.zerodeplibs.webpush.jwt.VAPIDJWTParam` object.

Also, by using `VAPIDJWTParam.getBuilder#additionalClaim`, you can specify arbitrary claims like the
following.

``` java

VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
    .resourceURLString("https://example.com")
    .expiresAfter(60, TimeUnit.SECONDS)
    .subject("mailto:test@example.com")
    .additionalClaim("myArbitraryClaim", "valueOfTheClaim") // Specifys an arbitrary claim.
    .build();

// This method internally calls VAPIDJWTGenerator#generate.
String value = vapidKeyPair.generateAuthorizationHeaderValue(param);
.....
```

This sub-module supports only `String`, `Boolean`,
`Integer`, `Long`, `Double`, `java.util.Date` and `java.time.Instant` as a type of additional claims.

So the following example doesn't work.

``` java

VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
    .resourceURLString("https://example.com")
    .expiresAfter(60, TimeUnit.SECONDS)
    .subject("mailto:test@example.com")
    .additionalClaim("myArbitraryClaim", new MyClaim("...."))
    .build();

// This method internally calls VAPIDJWTGenerator#generate.
String value = vapidKeyPair.generateAuthorizationHeaderValue(param);// An exception will be thrown.

```

## MISC

### Thread-safety

The implementations of this sub-module are thread-safe.

