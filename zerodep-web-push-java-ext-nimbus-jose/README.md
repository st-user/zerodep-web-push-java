# zerodep-web-push-java-ext-nimbus-jose

Provides an implementation for `com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator`
utilizing [Nimbus JOSE + JWT](https://connect2id.com/products/nimbus-jose-jwt).

## Requirements

The version of [com.nimbusds:nimbus-jose-jwt](https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt) must be
9.0 or higher(the latest version is recommended).

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
