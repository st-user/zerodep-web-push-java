# zerodep-web-push-java

A Java [Web Push](https://datatracker.ietf.org/doc/html/rfc8030) application server side library that has no dependencies on any specific third-party library.


- Provides the functionalities for [VAPID](https://datatracker.ietf.org/doc/html/rfc8292)
- Provides the functionalities for [Message Encryption for Web Push](https://datatracker.ietf.org/doc/html/rfc8291)
- Assumes that the [Push API](https://www.w3.org/TR/push-api/) is used.

This library doesn't work out of the box and JSON Web Token (JWT) functionality and Http Client functionality usually need to be provided externally.
However, you can choose arbitrary libraries that suit your project.


## The motivation for this project

The motivation for this project is to make it easy to implement Web Push functionality on any architecture, especially existing projects.

To achieve this, this project focuses on having no dependencies on any specific third-party library.


## Requirement

jdk 8+

(Jdk 9+ is needed to build from source.)

## 3rd party libraries

This project doesn't work out of the box. you have to have two kind of 3rd party libraries on your classpath
(Off course, it is possible to use the one you make yourself).


### JWT

 To create an `Authorization: vapid` header, you generate a JWT using libraries such as:

- Auth0
- JOSE4j


### HTTP Client

 For your application to send notifications to Push services, it makes HTTP requests to them.
 To do that, you utilize libraries such as:

- okHttp
- Apache HTTP Client
- JDK's HTTP Client(JDK 11+) 




## Installation

TBD 

Now the author is preparing the repositories on Github and Maven central.

## Usage examples

### spring boot


## License

MIT

## Contribution

