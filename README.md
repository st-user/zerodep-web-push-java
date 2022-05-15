# zerodep-web-push-java-example-vertx

A [zerodep-web-push-java](https://github.com/st-user/zerodep-web-push-java) example
using [Vert.x](https://vertx.io/docs/).

**This is the example for v2. The example for v1
is [here](https://github.com/st-user/zerodep-web-push-java-example-vertx/tree/main-v1).**

## Requirements

- JDK11+
- A library for generating ECDSA key pairs(e.g. [OpenSSL](https://www.openssl.org/))
- A browser supporting [Push API](https://developer.mozilla.org/en-US/docs/Web/API/Push_API)
  such as Google Chrome, Microsoft Edge and Firefox

## Usage

1. Set up and run the application.

   ```
   git clone https://github.com/st-user/zerodep-web-push-java-example-vertx.git
   cd zerodep-web-push-java-example-vertx
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
   ./mvnw compile exec:java
   ```

   **Windows**

   ```
   ./mvnw.cmd clean
   ./mvnw.cmd compile exec:java
   ```   


2. Open your browser and access `http://localhost:8080`;

3. Click the `subscribe` button.

4. Enter an arbitrary `message` and click the `send` button.

5. You should see a push notification!

6. (Optional) Since push notifications are handled in the background, we can get them even if we close the browser.

    - Close the browser and open a terminal instead.
    - Make the application send a push notification by using a command like the following:

   ```
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

## See also

- [zerodep-web-push-java-ext-jwt](https://github.com/st-user/zerodep-web-push-java-ext-jwt)
- [zerodep-web-push-java-example](https://github.com/st-user/zerodep-web-push-java-example)
- [zerodep-web-push-java-example-webflux](https://github.com/st-user/zerodep-web-push-java-example-webflux)

