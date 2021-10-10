# zerodep-web-push-java-example-vertx

**This example is currently under development.**

A [zerodep-web-push-java](https://github.com/st-user/zerodep-web-push-java) example
using [Vert.x](https://vertx.io/docs/).

## Requirements

- JDK8+
- Maven 3.1+
- A library for generating ECDSA key pairs(e.g. [OpenSSL](https://www.openssl.org/))

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
   mvn clean
   mvn compile exec:java
   ```

2. Open your browser and access `http://localhost:8080`;

3. Click the `subscribe` button.

4. Enter an arbitrary `message` and click the `send` button.

5. You should see a push notification!


