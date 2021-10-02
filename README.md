# zerodep-web-push-java-example

A [zerodep-web-push-java](https://github.com/st-user/zerodep-web-push-java) example
using [Spring Boot](https://spring.io/projects/spring-boot).

## Requirements

- JDK8+
- Maven 3.1+
- A library for generating ECDSA key pairs(e.g. [OpenSSL](https://www.openssl.org/))

## Usage

1. Set up and run the application.

   ```
   git clone ...
   cd zerodep-web-push-java-example
   mkdir .keys
   cd .keys
   openssl ecparam -genkey -name prime256v1 -noout -out my-private.pem
   openssl pkcs8 -in private.pem -topk8 -nocrypt -out my-private_pkcs8.pem
   openssl ec -in my-private.pem -pubout -conv_form uncompressed -out my-pub.pem
   cd ../
   mvn clean
   mvn spring-boot:run
   ```

2. Open your browser and access `http://localhost:8080`;

3. Click the `subscribe` button.

4. Enter an arbitrary `message` and click the `send` button.

5. You should see a push notification!

## NOTE

### private key

In general, it is not recommended storing the private key in plain text on the file system
(In the above example, we do that for simplicity).

However, such a method is still useful depending on architectures.

For example:

- [Using Secret Manager secrets in Cloud Run (Google Cloud)](https://cloud.google.com/run/docs/configuring/secrets)

  > Mount each secret as a volume, which makes the secret available to the container as files.
