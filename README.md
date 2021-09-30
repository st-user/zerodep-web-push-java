# zerodep-web-push-java

A Java [Web Push](https://datatracker.ietf.org/doc/html/rfc8030) application server side library
that has no dependencies on any specific third-party library.

- Provides the functionalities for [VAPID](https://datatracker.ietf.org/doc/html/rfc8292)
- Provides the functionalities
  for [Message Encryption for Web Push](https://datatracker.ietf.org/doc/html/rfc8291)
- Assumes that the [Push API](https://www.w3.org/TR/push-api/) is used.

This library itself does not provide all the functionality needed for Web Push.

The JSON Web Token (JWT) functionality and the Http Client functionality usually need to be provided
externally. However, you can choose arbitrary libraries that suit your project.

## The motivation for this project

The motivation for this project is to make it easy to implement Web Push functionality on any
architecture.

To achieve this, this project focuses on:

- having no dependencies on any specific third-party library.
- providing independent classes for each feature. For example, in this library, the `VAPIDKeyPair`
  interface and the `MessageEncryption` interface are defined and can be used independently.

## Requirement

JDK 8+

(To build from source, JDK 9+.)

## Third-party library

In order to implement the entire Web Push function with this library, at least the following two
types of functions must be provided from outside this library. Below are some examples of
third-party libraries.

(Off course, it is possible to use the one you make yourself).

### JWT

This kind of library is used to generate JSON Web Token (JWT)
for [VAPID](https://datatracker.ietf.org/doc/html/rfc8292).

- [Java JWT - auth0](https://github.com/auth0/java-jwt)
- [jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home)
- [Nimbus JOSE + JWT](https://connect2id.com/products/nimbus-jose-jwt)

### HTTP Client

Application servers need to make an HTTP Request in order to request the push service to deliver a
push message.

This kind of library is used to make HTTP Requests.

- [OkHttp](https://square.github.io/okhttp/)
- [Apache HTTPClient](https://hc.apache.org/httpcomponents-client-5.1.x/)
- [Java HTTP Client(JDK 11+)](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html)

## Installation

TBD

Now the author is preparing the repositories on Github and Maven central.

## Usage examples

### spring boot

TBD

## MISC

### Null safety

The public methods and constructors of this library do not accept `null`s and do not return `null`s.
They throw an `Exception` when a null reference is passed. The methods
return `java.util.Optional.empty()` when they need to indicate that the value does not exist.

The exceptions are:

- `com.zerodeplibs.webpush.PushSubscription.java`
- The runtime exceptions and checked exceptions thrown by the methods and constructors. For
  example, `getCause()` can return null.

### Thread safe or not thread safe

The classes and methods listed below can be called from multiple threads at the same time (thread
safe). However, the other classes and methods should **NOT** be considered thread-safe.

**Thread safe**

- The static utility methods(e.g. `com.zerodeplibs.webpush.header.Topic#ensure`).
- Implementations and instances that meet the conditions for thread safety described in javadoc(e.g.
  an instance of `com.zerodeplibs.webpush.VAPIDKeyPair.java`).

## License

MIT

## Contribution

TBD
