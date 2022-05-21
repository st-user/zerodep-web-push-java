# zerodep-web-push-java

A Java [Web Push](https://datatracker.ietf.org/doc/html/rfc8030) server-side library that can easily
be integrated with various third-party libraries and frameworks.

This library

- Provides the functionalities for [VAPID](https://datatracker.ietf.org/doc/html/rfc8292)
- Provides the functionalities
  for [Message Encryption for Web Push](https://datatracker.ietf.org/doc/html/rfc8291)
- Assumes that the [Push API](https://www.w3.org/TR/push-api/) is used

## Versions

| zerodep-web-push-java | java version requirements |
|-----------------------|---------------------------|
| v2.x.x                | java 11 or higher         |
| v1.x.x                | java 8 or higher          |

It is recommended that you use v2.x.x (the latest version of v2) if you can use java 11 or higher.
Some features are only available in version 2.

The documentation specific to v1 is [here](https://github.com/st-user/zerodep-web-push-java/tree/main-v1).


## Installation

``` xml

<dependency>
  <groupId>com.zerodeplibs</groupId>
  <artifactId>zerodep-web-push-java</artifactId>
  <version>2.0.2</version>
</dependency>

```

## How to use

Sending push notifications requires slightly complex steps. So it is recommended that you check one of the example projects(Please see [Examples](#examples)).

The following is a typical flow to send push notifications with this library.

1. Generate a key pair for VAPID with an arbitrary way(e.g. openssl commands).

    Example:
    ``` bash
    openssl ecparam -genkey -name prime256v1 -noout -out soruceKey.pem
    openssl pkcs8 -in soruceKey.pem -topk8 -nocrypt -out vapidPrivateKey.pem
    openssl ec -in sourceKey.pem -pubout -conv_form uncompressed -out vapidPublicKey.pem
    ```

2. Instantiate `VAPIDKeyPair` with the key pair generated in '1.'.

    Example:
    ``` java
    VAPIDKeyPair vapidKeyPair = VAPIDKeyPairs.of(
        PrivateKeySources.ofPEMFile(new File(pathToYourPrivateKeyFile).toPath()),
        PublicKeySources.ofPEMFile(new File(pathToYourPublicKeyFile).toPath()
    );
    ```

3. Send the public key for VAPID to the browser.

    Typically, this is achieved by exposing an endpoint to get the public key like `GET /getPublicKey`. Javascript on the browser fetches the public key through this endpoint.

    Example:
    ``` java
    @GetMapping("/getPublicKey")
    public byte[] getPublicKey() {
        return vapidKeyPair.extractPublicKeyInUncompressedForm();
    }
    ```
   (javascript on browser)
    ``` javascript
    const serverPublicKey = await fetch('/getPublicKey')
                                    .then(response => response.arrayBuffer());

    const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: serverPublicKey
    });
    ```

4. Obtain a push subscription from the browser.

    Typically, this is achieved by exposing an endpoint for the browser to post the push subscription like `POST /subscribe`.

    Example:
    ``` java
    @PostMapping("/subscribe")
    public void subscribe(@RequestBody PushSubscription subscription) {
       this.saveSubscriptionToStorage(subscription);
    }
    ```
   (javascript on browser)
    ``` javascript
    await fetch('/subscribe', {
        method: 'POST',
        body: JSON.stringify(subscription),
        headers: {
            'content-type': 'application/json'
        }
    }).then(res => {
       .....
    });
    ```
   

5. Send a push notification to the push service by using RequestPreparer (e.g. `StandardHttpClientRequestPreparer`) with the `VAPIDKeyPair` and the push subscription.

    ``` java
    HttpRequest request = StandardHttpClientRequestPreparer.getBuilder()
        .pushSubscription(subscription)
        .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
        .vapidJWTSubject("mailto:example@example.com")
        .pushMessage(message)
        .ttl(1, TimeUnit.HOURS)
        .urgencyLow()
        .topic("MyTopic")
        .build(vapidKeyPair)
        .toRequest();
   
    HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    ```


## Examples

### Spring Boot (MVC)

Source code and usage: [zerodep-web-push-java-example](https://github.com/st-user/zerodep-web-push-java-example)

<details>
    <summary><b>Controller for VAPID and Message Encryption</b></summary>
    
``` java
    
@Component
public class MyComponents {

    /**
     * In this example, we read a key pair for VAPID
     * from a PEM formatted file on the file system.
     * <p>
     * You can extract key pairs from various sources:
     * '.der' file(binary content), an octet sequence stored in a database and so on.
     * For more information, please see the javadoc of PrivateKeySources and PublicKeySources.
     */
    @Bean
    public VAPIDKeyPair vaidKeyPair(
        @Value("${private.key.file.path}") String privateKeyFilePath,
        @Value("${public.key.file.path}") String publicKeyFilePath) throws IOException {

        return VAPIDKeyPairs.of(
            PrivateKeySources.ofPEMFile(new File(privateKeyFilePath).toPath()),
            PublicKeySources.ofPEMFile(new File(publicKeyFilePath).toPath())

            /*
             * If you want to make your own VAPIDJWTGenerator,
             * the project for its sub-modules is a good example.
             * For more information, please consult the source codes on https://github.com/st-user/zerodep-web-push-java-ext-jwt
             */

            // (privateKey, publicKey) -> new MyOwnVAPIDJWTGenerator(privateKey)
        );
    }

}

    
@SpringBootApplication
@RestController
public class BasicExample {

    /**
     * @see MyComponents
     */
    @Autowired
    private VAPIDKeyPair vapidKeyPair;

    /**
     * # Step 1.
     * Sends the public key to user agents.
     * <p>
     * The user agents create a push subscription with this public key.
     */
    @GetMapping("/getPublicKey")
    public byte[] getPublicKey() {
        return vapidKeyPair.extractPublicKeyInUncompressedForm();
    }

    /**
     * # Step 2.
     * Obtains push subscriptions from user agents.
     * <p>
     * The application server(this application) requests the delivery of push messages with these subscriptions.
     */
    @PostMapping("/subscribe")
    public void subscribe(@RequestBody PushSubscription subscription) {
        this.saveSubscriptionToStorage(subscription);
    }

    /**
     * # Step 3.
     * Requests the delivery of push messages.
     * <p>
     * In this example, for simplicity and testability, we use an HTTP endpoint for this purpose.
     * However, in real applications, this feature doesn't have to be provided as an HTTP endpoint.
     */
    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(@RequestBody MyMessage myMessage)
        throws IOException, InterruptedException {

        String message = myMessage.getMessage();

        HttpClient httpClient = HttpClient.newBuilder().build();
        for (PushSubscription subscription : getSubscriptionsFromStorage()) {

            HttpRequest request = StandardHttpClientRequestPreparer.getBuilder()
                .pushSubscription(subscription)
                .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
                .vapidJWTSubject("mailto:example@example.com")
                .pushMessage(message)
                .ttl(1, TimeUnit.HOURS)
                .urgencyLow()
                .topic("MyTopic")
                .build(vapidKeyPair)
                .toRequest();

            // In this example, we send push messages in simple text format.
            // You can also send them in JSON format as follows:
            //
            // ObjectMapper objectMapper = (Create a new one or get from the DI container.)
            // ....
            // .pushMessage(objectMapper.writeValueAsBytes(objectForJson))
            // ....

            HttpResponse<String> httpResponse =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info(String.format("[Http Client] status code: %d", httpResponse.statusCode()));
            // 201 Created : Success!
            // 410 Gone : The subscription is no longer valid.
            // etc...
            // for more information, see the useful link below:
            // [Response from push service - The Web Push Protocol ](https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol)
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body("The message has been processed.");
    }
    
    ... Omitted for simplicity.
    
}
    
```
    
</details>

### Spring Boot (WebFlux)

Source code and usage: [zerodep-web-push-java-example-webflux](https://github.com/st-user/zerodep-web-push-java-example-webflux)

### Vert.x

Source code and usage: [zerodep-web-push-java-example-vertx](https://github.com/st-user/zerodep-web-push-java-example-vertx) 

<details>
    <summary><b>Standalone application for VAPID and Message Encryption</b></summary>

``` java

public class Example {

    /**
     * In this example, we read a key pair for VAPID
     * from a PEM formatted file on the file system.
     * <p>
     * You can extract key pairs from various sources:
     * '.der' file(binary content), an octet sequence stored in a database and so on.
     * For more information, please see the javadoc of PrivateKeySources and PublicKeySources.
     */
    private static VAPIDKeyPair createVAPIDKeyPair(Vertx vertx) throws IOException {
        return VAPIDKeyPairs.of(
            PrivateKeySources.ofPEMFile(new File("./.keys/my-private_pkcs8.pem").toPath()),
            PublicKeySources.ofPEMFile(new File("./.keys/my-pub.pem").toPath()),
            new VertxVAPIDJWTGeneratorFactory(() -> vertx));
    }

    public static void main(String[] args) throws IOException {

        Vertx vertx = Vertx.vertx();
        WebClient client = WebClient.create(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        VAPIDKeyPair vapidKeyPair = createVAPIDKeyPair(vertx);
        MockSubscriptionStorage mockStorage = new MockSubscriptionStorage();

        /*
         * # Step 1.
         * Sends the public key to user agents.
         *
         * The user agents create a push subscription with this public key.
         */
        router
            .get("/getPublicKey")
            .handler(ctx ->
                ctx.response()
                    .putHeader("Content-Type", "application/octet-stream")
                    .end(Buffer.buffer(vapidKeyPair.extractPublicKeyInUncompressedForm()))
            );

        /*
         * # Step 2.
         * Obtains push subscriptions from user agents.
         *
         * The application server(this application) requests the delivery of push messages with these subscriptions.
         */
        router
            .post("/subscribe")
            .handler(ctx -> {

                PushSubscription subscription =
                    ctx.getBodyAsJson().mapTo(PushSubscription.class);
                mockStorage.saveSubscriptionToStorage(subscription);

                ctx.response().end();
            });

        /*
         * # Step 3.
         * Requests the delivery of push messages.
         *
         * In this example, for simplicity and testability, we use an HTTP endpoint for this purpose.
         * However, in real applications, this feature doesn't have to be provided as an HTTP endpoint.
         */
        router
            .post("/sendMessage")
            .handler(ctx -> {

                String message = ctx.getBodyAsJson().getString("message");
                vertx.getOrCreateContext().put("messageToSend", new SampleMessageData(message));

                ExamplePushMessageDeliveryRequestProcessor processor =
                    new ExamplePushMessageDeliveryRequestProcessor(
                        vertx,
                        client,
                        vapidKeyPair,
                        mockStorage.getSubscriptionsFromStorage()
                    );
                processor.start();

                ctx.response()
                    .putHeader("Content-Type", "text/plain")
                    .end("Started sending notifications.");
            });

        router.route("/*").handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router).listen(8080, res -> {
            System.out.println("Vert.x HTTP server started.");
        });
    }

    /**
     * Sends HTTP requests to push services to request the delivery of push messages.
     * <p>
     * This class utilizes:
     * <ul>
     * <li>{@link Vertx#executeBlocking(Handler, Handler)} for the JWT creation and the message encryption.</li>
     * <li>{@link WebClient} for sending HTTP request asynchronously.</li>
     * </ul>
     */
    static class ExamplePushMessageDeliveryRequestProcessor {

        private final Vertx vertx;
        private final WebClient client;
        private final VAPIDKeyPair vapidKeyPair;
        private final List<PushSubscription> targetSubscriptions;

        private final int requestIntervalMillis;
        private final int connectionTimeoutMillis;

        ExamplePushMessageDeliveryRequestProcessor(
            Vertx vertx,
            WebClient client,
            VAPIDKeyPair vapidKeyPair,
            Collection<PushSubscription> targetSubscriptions) {

            this.vertx = vertx;
            this.client = client;
            this.vapidKeyPair = vapidKeyPair;
            this.targetSubscriptions = targetSubscriptions.stream().collect(Collectors.toList());
            this.requestIntervalMillis = 100;
            this.connectionTimeoutMillis = 10_000;
        }

        void start() {
            startInternal(0);
        }

        private void startInternal(int currentIndex) {

            PushSubscription subscription = targetSubscriptions.get(currentIndex);
            SampleMessageData messageData = vertx.getOrCreateContext().get("messageToSend");

            vertx.executeBlocking(promise -> {

                // In some circumstances, the JWT creation and the message encryption
                // may be considered "blocking" operations.
                //
                // On the author's environment, the JWT creation takes about 0.7ms
                // and the message encryption takes about 1.7ms.
                //
                // reference: https://vertx.io/docs/vertx-core/java/#golden_rule

                VertxWebClientRequestPreparer requestPreparer =
                    VertxWebClientRequestPreparer.getBuilder()
                        .pushSubscription(subscription)
                        .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
                        .vapidJWTSubject("mailto:example@example.com")
                        .pushMessage(messageData.getMessage())
                        .ttl(1, TimeUnit.HOURS)
                        .urgencyNormal()
                        .topic("MyTopic")
                        .build(vapidKeyPair);

                promise.complete(requestPreparer);

            }, res -> {

                VertxWebClientRequestPreparer requestPreparer =
                    (VertxWebClientRequestPreparer) res.result();
                requestPreparer.sendBuffer(
                    client,
                    req -> req.timeout(connectionTimeoutMillis),
                    httpResponseAsyncResult -> {

                        HttpResponse<Buffer> result = httpResponseAsyncResult.result();
                        System.out.println(
                            String.format("status code: %d", result.statusCode()));
                        // 201 Created : Success!
                        // 410 Gone : The subscription is no longer valid.
                        // etc...
                        // for more information, see the useful link below:
                        // [Response from push service - The Web Push Protocol ](https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol)

                    }
                );

            });

            if (currentIndex == targetSubscriptions.size() - 1) {
                return;
            }

            // In order to avoid wasting bandwidth,
            // we send HTTP requests at some intervals.
            vertx.setTimer(requestIntervalMillis, id -> startInternal(currentIndex + 1));
        }
    }
    
    ... Omitted for simplicity.
    
}

```

</details>

## Motivation

'zerodep-web-push-java' assumes that suitable implementations(libraries) of the following
functionalities vary depending on applications.

- Generating and signing JSON Web Token(JWT) used for VAPID
- Sending HTTP requests for the delivery of push messages
- Cryptographic operations

For example, an application may need to send HTTP requests **synchronously**
with [Apache HTTPClient](https://hc.apache.org/httpcomponents-client-5.1.x/) but another application
may need to do this **asynchronously** with [Vert.x](https://vertx.io/docs/vertx-web-client/java/).

In order to allow you to choose the way suitable for your application, this library doesn't force
your application to have dependencies on specifics libraries. Instead, this library

- Provides the functionality of JWT for VAPID
  with [sub-modules](https://github.com/st-user/zerodep-web-push-java-ext-jwt)
- Also, provides the functionality of JWT for VAPID out of the box(without any third-party library)
- Provides optional components helping applications use various third-party HTTP Client libraries
- Also, provides a component helping applications use [JDK's HTTP Client module](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html).
- Utilizes
  [the Java Cryptography Architecture (JCA)](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
  for cryptographic operations

Each of the sub-modules utilizes a specific JWT library. Each of the optional components supports a
specific HTTP Client library. you can choose suitable modules/components for your requirements. JCA
enables this library to be independent of specific implementations(providers) for security
functionality.

## Various sub-modules and helper components

The following functionalities can be provided from outside this library.

<details>
    <summary><b>JWT</b></summary>

JWT libraries are used to generate JSON Web Token (JWT)
for [VAPID](https://datatracker.ietf.org/doc/html/rfc8292).

Sub-modules for this functionality are available
from [zerodep-web-push-java-ext-jwt](https://github.com/st-user/zerodep-web-push-java-ext-jwt).

These sub-modules are optional.

</details>

<details>
    <summary><b>HTTP Client</b></summary>

Application servers need to send HTTP requests to push services in order to request the delivery of
push messages. Helper components for this functionality are available from
the `com.zerodeplibs.webpush.httpclient` package. One of them
utilizes [JDK's HTTP Client module](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html)
.
The others utilize third-party HTTP Client libraries. Supported third-party libraries are listed
below.

- [OkHttp](https://square.github.io/okhttp/)

  Version 4.9.0 or higher. The latest version is recommended.

- [Apache HTTPClient](https://hc.apache.org/httpcomponents-client-5.1.x/)

  Version 5.1 or higher. The latest version is recommended.

- [Eclipse Jetty Client Libraries](https://www.eclipse.org/jetty/documentation/jetty-11/programming-guide/index.html#pg-client)

    - Jetty 9: 9.4.33.v20201020 or higher.
    - Jetty 10: 10.0.0 or higher.
    - Jetty 11: 11.0.0 or higher.

  The latest versions are recommended.

- [Vert.x Web Client](https://vertx.io/docs/vertx-web-client/java/)

    - Vert.x 3: 3.9.2 or higher.
    - Vert.x 4: 4.0.0 or higher.

  The latest versions are recommended.

- **Others**
  
  'zerodep-web-push-java' doesn't directly provide optional components for the libraries other than the above. However, 'zerodep-web-push-java' can be easily integrated with the other HTTP Client libraries and frameworks.
  For example, you can also utilize the following libraries.

  - [Spring WebFlux (WebClient)](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client)
  - [Reactor Netty HTTP Client](https://projectreactor.io/docs/netty/release/reference/index.html#http-client)

  Please see [zerodep-web-push-java-example-webflux](https://github.com/st-user/zerodep-web-push-java-example-webflux) for more information.


</details>

## MISC

<details>
    <summary><b>Null safety</b></summary>

The public methods and constructors of this library do not accept `null`s and do not return `null`s.
They throw an `Exception` if a null reference is passed. Some methods
return `java.util.Optional.empty()` if they need to indicate that the value does not exist.

The exceptions are:

- `com.zerodeplibs.webpush.PushSubscription.java`. This is the server-side representation
  of [push subscription](https://www.w3.org/TR/push-api/#push-subscription).
- The methods of `Exception`. For example, their `getCause()` can return null.

</details>


<details>
    <summary><b>Working with Java Cryptography Architecture(JCA)</b></summary>

This library
uses [the Java Cryptography Architecture (JCA)](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
API for cryptographic operations. The algorithms used by this library are listed below.

``` java
java.security.SecureRandom
java.security.KeyFactory.getInstance("EC") 
java.security.KeyPairGenerator.getInstance("EC") // curve: secp256r1
java.security.Signature.getInstance("SHA256withECDSA")
javax.crypto.KeyAgreement.getInstance("ECDH")
javax.crypto.Mac.getInstance("HmacSHA256") 
javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
```

By default, the providers shipped with the JDK will be used(e.g. `SunEC` and `SunJCE`).

Of course, any provider that supports these algorithms is available(
e.g. [Bouncy Castle](https://bouncycastle.org/)). This is because 'zerodep-web-push-java' has no
dependencies on any specific provider.

</details>

## License

MIT

## Contribution

This project follows a [git flow](https://nvie.com/posts/a-successful-git-branching-model/) -style
model.

Please open pull requests against the `dev` branch.
