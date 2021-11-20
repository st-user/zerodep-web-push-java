package org.example;

import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.VAPIDKeyPair;
import com.zerodeplibs.webpush.VAPIDKeyPairs;
import com.zerodeplibs.webpush.ext.jwt.vertx.VertxVAPIDJWTGeneratorFactory;
import com.zerodeplibs.webpush.httpclient.VertxWebClientRequestPreparer;
import com.zerodeplibs.webpush.key.PrivateKeySources;
import com.zerodeplibs.webpush.key.PublicKeySources;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private static class MockSubscriptionStorage {
        private final Map<String, PushSubscription> subscriptionMap = new ConcurrentHashMap<>();

        Collection<PushSubscription> getSubscriptionsFromStorage() {
            return this.subscriptionMap.values();
        }

        void saveSubscriptionToStorage(PushSubscription subscription) {
            this.subscriptionMap.put(subscription.getEndpoint(), subscription);
        }
    }

    private static class SampleMessageData {

        private final String message;

        SampleMessageData(String message) {
            message = message == null ? "" : message.trim();
            this.message = message.length() == 0 ? "Default Message." : message;
        }

        public String getMessage() {
            return message;
        }
    }
}
