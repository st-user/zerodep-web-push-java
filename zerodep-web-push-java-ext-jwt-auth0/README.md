# zerodep-web-push-java-ext-jwt-auth0

Provides an implementation for `com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator`
utilizing [Java JWT - auth0](https://github.com/auth0/java-jwt).

## Requirements

The version of [com.auth0:java-jwt](https://mvnrepository.com/artifact/com.auth0/java-jwt) must be
3.17.0 or higher(the latest version is recommended).

## Usage

### pom.xml

You can use this sub-module by adding the dependency to your pom.xml.

``` xml
<dependency>
    <groupId>com.zerodeplibs</groupId>
    <artifactId>zerodep-web-push-java</artifactId>
    <version>1.3.2</version>
</dependency>
<dependency>
    <groupId>com.zerodeplibs</groupId>
    <artifactId>zerodep-web-push-java-ext-jwt-auth0</artifactId>
    <version>1.3.2</version>
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
    .expiresAfterSeconds(60)
    .subject("mailto:test@example.com")
    .additionalClaim("myArbitraryClaim", "valueOfTheClaim") // Specifys an arbitrary claim.
    .build();

// This method internally calls VAPIDJWTGenerator#generate.
String value = vapidKeyPair.generateAuthorizationHeaderValue(param); 
.....
```

The underlying Auth0's library can support only `Map`, `List`, `Boolean`,
`Integer`, `Long`, `Double`, `String` and `Date` as a type of claim.

So the following example doesn't work.

``` java

VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
    .resourceURLString("https://example.com")
    .expiresAfterSeconds(60)
    .subject("mailto:test@example.com")
    .additionalClaim("myArbitraryClaim", new MyClaim("...."))
    .build();

// This method internally calls VAPIDJWTGenerator#generate.
String value = vapidKeyPair.generateAuthorizationHeaderValue(param);// An exception will be thrown.
```

For more information, please consult the javadoc
of `com.auth0.jwt.JWTCreator.Builder#withPayload(java.util.Map<String, ?>)`.

## MISC

### Thread-safety

The implementations of this sub-module are thread-safe.
