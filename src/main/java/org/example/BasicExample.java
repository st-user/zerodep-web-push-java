package org.example;

import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.VAPIDKeyPair;
import com.zerodeplibs.webpush.VAPIDKeyPairs;
import com.zerodeplibs.webpush.httpclient.OkHttpClientRequestPreparer;
import com.zerodeplibs.webpush.key.PrivateKeySources;
import com.zerodeplibs.webpush.key.PublicKeySources;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> sendMessage(@RequestBody MyMessage myMessage)
        throws IOException {

        String message = myMessage.getMessage();

        OkHttpClient httpClient = new OkHttpClient();
        for (PushSubscription subscription : getSubscriptionsFromStorage()) {

            Request request = OkHttpClientRequestPreparer.getBuilder()
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
            // But you can also send them in JSON format as follows:
            //
            // ObjectMapper objectMapper = (Create a new one or get from the DI container.)
            // ....
            // pushMessage(objectMapper.writeValueAsBytes(objectForJson))
            // ....

            try (Response response = httpClient.newCall(request).execute()) {
                logger.info(String.format("[OkHttp] status code: %d", response.code()));
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


    // "/sendMessage" endpoint
    // utilizing [Apache HTTP Client](https://hc.apache.org/httpcomponents-client-5.1.x/).
    /*
    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessageWithApacheHttpClient(@RequestBody MyMessage myMessage)
        throws IOException {

        String message = myMessage.getMessage();
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            for (PushSubscription subscription : getSubscriptionsFromStorage()) {

                HttpPost httpPost = ApacheHttpClientRequestPreparer.getBuilder()
                    .pushSubscription(subscription)
                    .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
                    .vapidJWTSubject("mailto:example@example.com")
                    .pushMessage(message)
                    .ttl(1, TimeUnit.HOURS)
                    .urgencyLow()
                    .topic("MyTopic")
                    .build(vapidKeyPair)
                    .toHttpPost();

                try (CloseableHttpResponse response = client.execute(httpPost)) {
                    logger.info(String.format("[Apache Http Client] status code: %d", response.getCode()));
                }
            }
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body("The message has been processed.");
    }
    */

    // The asynchronous version of "/sendMessage" endpoint
    // utilizing [Apache HTTP Client](https://hc.apache.org/httpcomponents-client-5.1.x/).
    /*
    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessageWithApacheHttpClientAsync(
        @RequestBody MyMessage myMessage)
        throws IOException, ExecutionException, InterruptedException {

        String message = myMessage.getMessage();
        try (CloseableHttpAsyncClient client = HttpAsyncClients.custom()
            .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_1)
            .build()) {

            client.start();

            for (PushSubscription subscription : getSubscriptionsFromStorage()) {

                SimpleHttpRequest request = ApacheHttpClientRequestPreparer.getBuilder()
                    .pushSubscription(subscription)
                    .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
                    .vapidJWTSubject("mailto:example@example.com")
                    .pushMessage(message)
                    .ttl(1, TimeUnit.HOURS)
                    .urgencyLow()
                    .topic("MyTopic")
                    .build(vapidKeyPair)
                    .toSimpleHttpRequest();

                client.execute(
                    SimpleRequestProducer.create(request),
                    SimpleResponseConsumer.create(),
                    new FutureCallback<SimpleHttpResponse>() {
                        @Override
                        public void completed(SimpleHttpResponse response) {
                            logger.info(String.format("[Apache Http Client(async)] status code: %d",
                                response.getCode()));
                        }

                        @Override
                        public void failed(Exception e) {
                            logger.error("failed", e);
                        }

                        @Override
                        public void cancelled() {
                            logger.warn("cancelled");
                        }
                    }).get();

            }

        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body("The message has been processed.");
    }
    */


    // "/sendMessage" endpoint
    // utilizing [Jetty Client](https://www.eclipse.org/jetty/documentation/jetty-11/programming-guide/index.html#pg-client).
    /*
    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessageWithJettyHttpClient(@RequestBody MyMessage myMessage)
        throws Exception {

        String message = myMessage.getMessage();


        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();

        HttpClient httpClient = new HttpClient(sslContextFactory);
        // HttpClient httpClient = new HttpClient(); // !! When using Jetty 10 or higher.

        // From version 10, 'HttpClient supports HTTPS requests out-of-the-box like a browser does.'
        // see also:
        // https://www.eclipse.org/jetty/documentation/jetty-9/index.html#http-client
        // https://www.eclipse.org/jetty/documentation/jetty-10/programming-guide/index.html#pg-client-http-configuration-tls

        httpClient.start();
        for (PushSubscription subscription : getSubscriptionsFromStorage()) {

            org.eclipse.jetty.client.api.Request request =
                JettyHttpClientRequestPreparer.getBuilder()
                    .pushSubscription(subscription)
                    .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
                    .vapidJWTSubject("mailto:example@example.com")
                    .pushMessage(message)
                    .ttl(1, TimeUnit.HOURS)
                    .urgencyLow()
                    .topic("MyTopic")
                    .build(vapidKeyPair)
                    .toRequest(httpClient);

            ContentResponse contentResponse = request.send();
            logger.info(
                String.format("[Jetty Http Client] status code: %d", contentResponse.getStatus()));

        }
        new Thread(() -> LifeCycle.stop(httpClient)).start();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body("The message has been processed.");
    }
    */


    // "/sendMessage" endpoint utilizing JDK Http Client(JDK11+ required).
    // When using this endpoint, remove the comment outs in 'Jdk11HttpClientRequestPreparer.java'.
    // 'Jdk11HttpClientRequestPreparer.java' is in the same directory as this class.
    /*
    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessageWithJdk11HttpClient(@RequestBody MyMessage myMessage)
        throws IOException, InterruptedException {

        String message = myMessage.getMessage();

        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .build();

        for (PushSubscription subscription : getSubscriptionsFromStorage()) {

            HttpRequest httpRequest = Jdk11HttpClientRequestPreparer.getBuilder()
                .pushSubscription(subscription)
                .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
                .vapidJWTSubject("mailto:example@example.com")
                .pushMessage(message)
                .ttl(1, TimeUnit.HOURS)
                .urgencyLow()
                .topic("MyTopic")
                .build(vapidKeyPair)
                .toRequest();

            HttpResponse<String> httpResponse =
                httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            logger.info(String.format("[JDK11 Http Client] status code: %d", httpResponse.statusCode()));
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body("The message has been processed.");
    }
    */

    private Collection<PushSubscription> getSubscriptionsFromStorage() {
        return this.subscriptionMap.values();
    }

    private void saveSubscriptionToStorage(PushSubscription subscription) {
        this.subscriptionMap.put(subscription.getEndpoint(), subscription);
    }

    private final Logger logger = LoggerFactory.getLogger(BasicExample.class);
    private final Map<String, PushSubscription> subscriptionMap = new HashMap<>();

    static class MyMessage {

        private String message;

        public String getMessage() {
            String message = this.message == null ? "" : this.message.trim();
            return message.length() == 0 ? "Default Message." : message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
