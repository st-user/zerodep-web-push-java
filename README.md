# zerodep-web-push-java

A Java [Web Push](https://datatracker.ietf.org/doc/html/rfc8030) server-side library that doesn't
force your application to have dependencies on specific third-party libraries.

- Provides the functionalities for [VAPID](https://datatracker.ietf.org/doc/html/rfc8292)
- Provides the functionalities
  for [Message Encryption for Web Push](https://datatracker.ietf.org/doc/html/rfc8291)
- Assumes that the [Push API](https://www.w3.org/TR/push-api/) is used.

This library itself doesn't provide all the functionalities needed for Web Push.

The JSON Web Token (JWT) functionality and the Http Client functionality need to be provided
externally. However, you can choose arbitrary libraries that suit your project.

**NOTE**

- Sub-modules that help applications utilize a third-party JWT library are available
  from [zerodep-web-push-java-ext-jwt](https://github.com/st-user/zerodep-web-push-java-ext-jwt).

- Components that help applications utilize a third-party HTTP Client library are available in '
  zerodep-web-push-java' out of the box.

If your application can utilize these sub-modules/components, they make it easier for you to
implement the related features.

These sub-modules and components are optional, so they don't force your application to have
dependencies on specific third-party libraries.

## The motivation for this project

The motivation for this project is to make it easy to implement Web Push functionality on any
architecture(e.g. an existing project that already depends on a specific web-framework and various
third-party libraries).

To achieve this, this project focuses on:

- having no dependencies on any specific third-party library.
- providing an independent component for each feature. For example, in this library,
  the `VAPIDKeyPair`
  interface and the `MessageEncryption` interface are defined and can be used independently.

## Requirements

Java 8+

(To build from source, JDK 9+)

## Installation

``` xml

<dependency>
  <groupId>com.zerodeplibs</groupId>
  <artifactId>zerodep-web-push-java</artifactId>
  <version>1.2.0</version>
</dependency>

```

## Sub-modules and third-party libraries

In order to implement the complete Web Push functionality with this library, at least the following
two types of functionalities have to be provided from outside this library.

<details>
    <summary><b>JWT</b></summary>

A JWT library is used to generate JSON Web Token (JWT)
for [VAPID](https://datatracker.ietf.org/doc/html/rfc8292).

Sub-modules for this functionality are available
from [zerodep-web-push-java-ext-jwt](https://github.com/st-user/zerodep-web-push-java-ext-jwt).

These sub-modules are optional, so you can also implement such functionality by yourself by using classes and interfaces
in `com.zerodeplibs.webpush.jwt` package.

</details>

<details>
    <summary><b>HTTP Client</b></summary>

Application servers need to send HTTP requests to push services in order to request the delivery of push
messages. Helper components for this functionality are available from
the `com.zerodeplibs.webpush.httpclient` package. Each of these helper components
utilizes a third-party HTTP Client library. Supported libraries are listed below.

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

These components and their dependencies are optional, so you can also implement such functionality by yourself by using classes in `com.zerodeplibs.webpush.httpclient` package.

</details>

## Usage examples

### Spring Boot

full source
code: [zerodep-web-push-java-example](https://github.com/st-user/zerodep-web-push-java-example)

<details>
    <summary><b>Controller for VAPID and Message Encryption</b></summary>

``` java

@SpringBootApplication
@RestController
public class BasicExample {

    @Autowired
    private VAPIDKeyPair vapidKeyPair;

    /**
     * In this example, we read the key pair for VAPID
     * from a PEM formatted file on the file system.
     * <p>
     * You can extract key pairs from various sources.
     * For example, '.der' file(binary content), an octet sequence stored in a database and so on.
     * Please see the javadoc of PrivateKeySources and PublicKeySources.
     */
    @Bean
    public VAPIDKeyPair vaidKeyPair(
        @Value("${private.key.file.path}") String privateKeyFilePath,
        @Value("${public.key.file.path}") String publicKeyFilePath) throws IOException {

        return VAPIDKeyPairs.of(
            PrivateKeySources.ofPEMFile(new File(privateKeyFilePath).toPath()),
            PublicKeySources.ofPEMFile(new File(publicKeyFilePath).toPath())

            /*
             * If you want to implement VAPIDJWTGenerator yourself,
             * the project for its sub-modules is a good example.
             * For more information, please consult the source codes on https://github.com/st-user/zerodep-web-push-java-ext-jwt
             */

            // (privateKey, publicKey) -> new MyOwnVAPIDJWTGenerator(privateKey)
        );
    }

    /**
     * # Step 1.
     * Sends the public key to user agents.
     * <p>
     * The user agents create push subscriptions with this public key.
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
     * In this example, for simplicity and testability, we implement this feature as an HTTP endpoint.
     * However, in real applications, this feature does not have to be an HTTP endpoint.
     */
    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> messages)
        throws IOException {

        String message = messages.getOrDefault("message", "").trim();
        message = message.length() == 0 ? "Default Message." : message;

        OkHttpClient httpClient = new OkHttpClient();
        MessageEncryption messageEncryption = MessageEncryptions.of();

        for (PushSubscription subscription : getSubscriptionsFromStorage()) {

            VAPIDJWTParam vapidjwtParam = VAPIDJWTParam.getBuilder()
                .resourceURLString(subscription.getEndpoint())
                .expiresAfterSeconds((int) TimeUnit.MINUTES.toSeconds(15))
                .subject("mailto:example@example.com")
                .build();

            PushMessage pushMessage = PushMessage.ofUTF8(message);

            // In this example, we send push messages in simple text format.
            // But you can also send them in JSON format as follows:
            //
            // ObjectMapper objectMapper = (Create a new one or get from the DI container.)
            // PushMessage pushMessage = PushMessage.of(objectMapper.writeValueAsBytes(objectForJson));

            EncryptedPushMessage encryptedPushMessage = messageEncryption.encrypt(
                UserAgentMessageEncryptionKeyInfo.from(subscription.getKeys()),
                pushMessage
            );

            Request request = new Request.Builder()
                .url(subscription.getEndpoint())
                .addHeader("Authorization",
                    vapidKeyPair.generateAuthorizationHeaderValue(vapidjwtParam))
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Content-Encoding", encryptedPushMessage.contentEncoding())
                .addHeader("TTL", String.valueOf(TTL.hours(1)))
                .addHeader("Urgency", Urgency.low())
                .addHeader("Topic", Topic.ensure("myTopic"))
                // Depending on HTTP Client libraries, you may have to set "Content-Length" manually.
                // .addHeader("Content-Length", String.valueOf(encryptedPushMessage.length()))
                .post(okhttp3.RequestBody.create(encryptedPushMessage.toBytes()))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                logger.info(String.format("status code: %d", response.code()));
                // 201 Created : Success!
                // 410 Gone : The subscription is no longer valid.
                // etc...
                // for more information, see the useful link below:
                // [Response from push service - The Web Push Protocol ](https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol)
            }

        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body("The message has been processed.");
    }

    private Collection<PushSubscription> getSubscriptionsFromStorage() {
        return this.subscriptionMap.values();
    }

    private void saveSubscriptionToStorage(PushSubscription subscription) {
        this.subscriptionMap.put(subscription.getEndpoint(), subscription);
    }

    private final Logger logger = LoggerFactory.getLogger(BasicExample.class);
    private final Map<String, PushSubscription> subscriptionMap = new HashMap<>();

}

```

</details>

### Vert.x

full source
code: [zerodep-web-push-java-example-vertx](https://github.com/st-user/zerodep-web-push-java-example-vertx)

<details>
    <summary><b>Standalone application for VAPID and Message Encryption</b></summary>

``` java

public class Example {

    /**
     * In this example, we read the key pair for VAPID
     * from a PEM formatted file on the file system.
     * <p>
     * You can extract key pairs from various sources.
     * For example, '.der' file(binary content), an octet sequence stored in a database and so on.
     * Please see the javadoc of PrivateKeySources and PublicKeySources.
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
         * The user agents create push subscriptions with this public key.
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
         * In this example, for simplicity and testability, we implement this feature as an HTTP endpoint.
         * However, in real applications, this feature does not have to be an HTTP endpoint.
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

                VAPIDJWTParam vapidjwtParam = VAPIDJWTParam.getBuilder()
                    .resourceURLString(subscription.getEndpoint())
                    .expiresAfterSeconds((int) TimeUnit.MINUTES.toSeconds(15))
                    .subject("mailto:example@example.com")
                    .build();
                String jwt = vapidKeyPair.generateAuthorizationHeaderValue(vapidjwtParam);

                MessageEncryption messageEncryption = MessageEncryptions.of();
                EncryptedPushMessage encryptedPushMessage = messageEncryption.encrypt(
                    UserAgentMessageEncryptionKeyInfo.from(subscription.getKeys()),
                    // In this example, we send push messages in simple text format.
                    // But you can also send them in JSON format.
                    PushMessage.ofUTF8(messageData.getMessage())
                );


                promise.complete(new JWTAndMessage(jwt, encryptedPushMessage));

            }, res -> {

                JWTAndMessage jwtAndMessage = (JWTAndMessage) res.result();
                EncryptedPushMessage encryptedPushMessage = jwtAndMessage.encryptedPushMessage;

                client
                    .postAbs(subscription.getEndpoint())
                    .timeout(connectionTimeoutMillis)
                    .putHeader("Authorization", jwtAndMessage.jwt)
                    .putHeader("Content-Type", "application/octet-stream")
                    .putHeader("Content-Encoding", encryptedPushMessage.contentEncoding())
                    .putHeader("TTL", String.valueOf(TTL.hours(1)))
                    .putHeader("Urgency", Urgency.normal())
                    .putHeader("Topic", Topic.ensure("myTopic"))
                    .sendBuffer(Buffer.buffer(encryptedPushMessage.toBytes()),
                        httpResponseAsyncResult -> {

                            HttpResponse<Buffer> result = httpResponseAsyncResult.result();
                            System.out.println(
                                String.format("status code: %d", result.statusCode()));
                            // 201 Created : Success!
                            // 410 Gone : The subscription is no longer valid.
                            // etc...
                            // for more information, see the useful link below:
                            // [Response from push service - The Web Push Protocol ](https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol)

                        });

            });

            if (currentIndex == targetSubscriptions.size() - 1) {
                return;
            }

            // In order to avoid wasting bandwidth,
            // we send HTTP requests at some intervals.
            vertx.setTimer(requestIntervalMillis, id -> startInternal(currentIndex + 1));
        }

        private static class JWTAndMessage {

            final String jwt;
            final EncryptedPushMessage encryptedPushMessage;

            JWTAndMessage(String jwt, EncryptedPushMessage encryptedPushMessage) {
                this.jwt = jwt;
                this.encryptedPushMessage = encryptedPushMessage;
            }
        }
    }

    ... Omitted for simplicity.
}

```

</details>

## MISC

<details>
    <summary><b>Null safety</b></summary>

The public methods and constructors of this library do not accept `null`s and do not return `null`s.
They throw an `Exception` when a null reference is passed. Some methods
return `java.util.Optional.empty()` when they need to indicate that the value does not exist.

The exceptions are:

- `com.zerodeplibs.webpush.PushSubscription.java`(the server-side representation for
  a [push subscription](https://www.w3.org/TR/push-api/#push-subscription)).
- The methods of runtime exceptions and checked exceptions thrown by some methods and constructors.
  For example, their `getCause()` can return null.

</details>


<details>
    <summary><b>Thread safe or not thread safe</b></summary>

The methods listed below can be called from multiple threads at the same time (thread safe).
However, the others should **NOT** be considered thread-safe.

**Thread safe**

- The static utility methods(e.g. `com.zerodeplibs.webpush.header.Topic#ensure`).
- The methods of instances that meet the conditions for thread safety described in their javadoc(
  e.g. an instance of `com.zerodeplibs.webpush.VAPIDKeyPair.java`).

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
javax.crypto.KeyAgreement.getInstance("ECDH")
javax.crypto.Mac.getInstance("HmacSHA256") 
javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
```

By default, the providers shipped with the JDK will be used(e.g. `SunEC` and `SunJCE`).

Of course, any provider that supports these algorithms is available(
e.g. [Bouncy Castle](https://bouncycastle.org/)). This is because 'zerodep-web-push-java' has no dependencies
on any specific provider.

</details>

## License

MIT

## Contribution

This project follows a [git flow](https://nvie.com/posts/a-successful-git-branching-model/) -style
model.

Please open pull requests against the `dev` branch.
