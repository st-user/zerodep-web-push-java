# zerodep-web-push-java-example-webflux

A [zerodep-web-push-java](https://github.com/st-user/zerodep-web-push-java) example
using [Spring WebFlux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html).

Also, this example demonstrates how to work
with [Reactor Netty HTTP Client](https://projectreactor.io/docs/netty/release/reference/index.html#http-client)

This example is sightly more complex but practical compared
to [basic](../basic)
and [vertx](../vertx).

In this example, we demonstrate the error handling on requesting the delivery of push notifications.

**This is the example for v2. The example for v1 is [here](https://github.com/st-user/zerodep-web-push-java/tree/main-v1).**

## Requirements

- JDK17+
- A library for generating ECDSA key pairs(e.g. [OpenSSL](https://www.openssl.org/))
- A browser supporting [Push API](https://developer.mozilla.org/en-US/docs/Web/API/Push_API)
  such as Google Chrome, Microsoft Edge and Firefox

## Usage

1. Set up and run the application.

   ```
   git clone https://github.com/st-user/zerodep-web-push-java.git
   cd examples/webflux
   mkdir .keys
   cd .keys
   openssl ecparam -genkey -name prime256v1 -noout -out my-private.pem
   openssl pkcs8 -in my-private.pem -topk8 -nocrypt -out my-private_pkcs8.pem
   openssl ec -in my-private.pem -pubout -conv_form uncompressed -out my-pub.pem
   cd ../
   ```

   **Linux/mac OS**

   ```
   ./mvnw clean
   ./mvnw spring-boot:run
   ```

   **Windows**

   ```
   ./mvnw.cmd clean
   ./mvnw.cmd spring-boot:run
   ```


2. Open your browser and access `http://localhost:8080`;

3. Click the `subscribe` button.

4. Enter an arbitrary `message` and click the `send` button.

5. You should see a push notification!

6. (Optional) Since push notifications are handled in the background, we can get them even if we close the browser.

    - Close the browser and open a terminal instead.
    - Make the application send a push notification by using a command like the following:

   ``` bash
   curl -X POST http://localhost:8080/sendMessage \  
   -H 'Content-Type: application/json' \
   -d '{ "message": "Message sent with curl." }'
   ```

## NOTE

### private key

In general, storing private keys in plain text on a file system is not recommended
(In the above example, we do that for simplicity).

However, such a method is still useful depending on architectures.

For example:

- [Using Secret Manager secrets in Cloud Run (Google Cloud)](https://cloud.google.com/run/docs/configuring/secrets)

  > Mount each secret as a volume, which makes the secret available to the container as files.

## Further reading

- [Push API - Web APIs | MDN](https://developer.mozilla.org/en-US/docs/Web/API/Push_API)
- [Web Push Notifications: Timely, Relevant, and Precise](https://developers.google.com/web/fundamentals/push-notifications)
