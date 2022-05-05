# zerodep-web-push-java-ext-jwt

Sub-modules for [zerodep-web-push-java](https://github.com/st-user/zerodep-web-push-java) that
provide implementations of `com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator` utilizing third-party JWT
libraries.

## Versions

This is the branch for v2. The branch for v1 is [here](https://github.com/st-user/zerodep-web-push-java-ext-jwt/tree/main-v1). 


## [zerodep-web-push-java-ext-jwt-auth0](./zerodep-web-push-java-ext-jwt-auth0/README.md)

Uses [Java JWT - auth0](https://github.com/auth0/java-jwt).

## [zerodep-web-push-java-ext-jwt-fusionauth](./zerodep-web-push-java-ext-jwt-fusionauth/README.md)

Uses [FusionAuth JWT](https://github.com/fusionauth/fusionauth-jwt).

## [zerodep-web-push-java-ext-jwt-jjwt](./zerodep-web-push-java-ext-jwt-jjwt/README.md)

Uses [Java JWT](https://github.com/jwtk/jjwt).

## [zerodep-web-push-java-ext-jwt-jose4j](./zerodep-web-push-java-ext-jwt-jose4j/README.md)

Uses [jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home).

## [zerodep-web-push-java-ext-jwt-nimbus-jose](./zerodep-web-push-java-ext-jwt-nimbus-jose/README.md)

Uses [Nimbus JOSE + JWT](https://connect2id.com/products/nimbus-jose-jwt).

## [zerodep-web-push-java-ext-jwt-vertx](./zerodep-web-push-java-ext-jwt-vertx/README.md)

Uses [JWT Auth - Vert.x](https://vertx.io/docs/vertx-auth-jwt/java/).

## License

MIT

## MISC

### Thread-safety

- The classes of the sub-modules other
  than [zerodep-web-push-java-ext-jwt-vertx](./zerodep-web-push-java-ext-jwt-vertx/README.md) are
  always thread-safe.
- The classes
  of [zerodep-web-push-java-ext-jwt-vertx](./zerodep-web-push-java-ext-jwt-vertx/README.md) are
  thread-safe when a given `vertxObtainStrategy` is thread-safe.

## Contribution

This project follows a [git flow](https://nvie.com/posts/a-successful-git-branching-model/) -style
model.

Please open pull requests against the `dev` branch.
