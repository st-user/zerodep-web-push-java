# zerodep-web-push-java-ext-fusionauth

Provides an implementation for `com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator`
utilizing [FusionAuth JWT](https://github.com/fusionauth/fusionauth-jwt).

## Requirements

The recommended version
of [io.fusionauth:fusionauth-jwt](https://mvnrepository.com/artifact/io.fusionauth/fusionauth-jwt)
is 4.0.0 or higher(The latest version is more desirable).

## Usage

### pom.xml

You can use this sub-module by adding the dependency to your pom.xml.

```

TBD

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
