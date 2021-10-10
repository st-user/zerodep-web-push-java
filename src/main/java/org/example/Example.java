package org.example;

import com.zerodeplibs.webpush.EncryptedPushMessage;
import com.zerodeplibs.webpush.MessageEncryption;
import com.zerodeplibs.webpush.MessageEncryptions;
import com.zerodeplibs.webpush.PushMessage;
import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.UserAgentMessageEncryptionKeyInfo;
import com.zerodeplibs.webpush.VAPIDKeyPair;
import com.zerodeplibs.webpush.VAPIDKeyPairs;
import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.PrivateKeySources;
import com.zerodeplibs.webpush.key.PublicKeySources;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            (privateKey, publicKey) -> new MyVertxVAPIDJWTGenerator(vertx, privateKey));
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
            .respond(
                ctx -> ctx
                    .response()
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

                new RequestWorker(
                    vertx, client, vapidKeyPair, mockStorage.getSubscriptionsFromStorage()
                ).start();

                ctx.response()
                    .putHeader("Content-Type", "text/plain")
                    .end("Started sending notifications.");
            });

        router.route("/*").handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router).listen(8080, res -> {
            System.out.println("Vert.x HTTP server started.");
        });
    }

    static class RequestWorker {

        private final Vertx vertx;
        private final WebClient client;
        private final VAPIDKeyPair vapidKeyPair;
        private final List<PushSubscription> targetSubscriptions;

        private final Map<String, AtomicLong> requestingCountPerOrigin;

        private final int requestIntervalMillis;
        private final int maxConcurrencyPerOrigin;
        private final int retryTimerMillis;
        private final int connectionTimeoutMillis;

        private static final Pattern URL_ORIGIN_PATTERN = Pattern.compile("^(.+://.+?)/.*$");

        RequestWorker(
            Vertx vertx,
            WebClient client,
            VAPIDKeyPair vapidKeyPair,
            Collection<PushSubscription> targetSubscriptions) {

            this.vertx = vertx;
            this.client = client;
            this.vapidKeyPair = vapidKeyPair;
            this.targetSubscriptions = targetSubscriptions.stream().collect(Collectors.toList());
            this.requestIntervalMillis = 100;
            this.maxConcurrencyPerOrigin = 5;
            this.retryTimerMillis = 500;
            this.connectionTimeoutMillis = 10_000;

            this.requestingCountPerOrigin = new ConcurrentHashMap<>();
        }

        void start() {
            startInternal(0);
        }

        private void startInternal(int currentIndex) {

            PushSubscription subscription = targetSubscriptions.get(currentIndex);

            AtomicLong requestingCount =
                requestingCountPerOrigin.computeIfAbsent(extractOrigin(subscription),
                    k -> new AtomicLong());

            if (requestingCount.intValue() >= maxConcurrencyPerOrigin) {
                vertx.setTimer(retryTimerMillis, id -> startInternal(currentIndex));
                return;
            }
            SampleMessageData messageData = vertx.getOrCreateContext().get("messageToSend");

            vertx.executeBlocking(promise -> {

                // In this example, we send push messages in simple text format.
                // But you can also send them in JSON format.

                MessageEncryption messageEncryption = MessageEncryptions.of();
                EncryptedPushMessage encryptedPushMessage = messageEncryption.encrypt(
                    UserAgentMessageEncryptionKeyInfo.from(subscription.getKeys()),
                    PushMessage.ofUTF8(messageData.getMessage())
                );

                VAPIDJWTParam vapidjwtParam = VAPIDJWTParam.getBuilder()
                    .resourceURLString(subscription.getEndpoint())
                    .expiresAfterSeconds((int) TimeUnit.MINUTES.toSeconds(15))
                    .subject("mailto:example@example.com")
                    .build();
                String jwt = vapidKeyPair.generateAuthorizationHeaderValue(vapidjwtParam);

                promise.complete(new MessageAndJWT(encryptedPushMessage, jwt));

            }, res -> {

                MessageAndJWT messageAndJWT = (MessageAndJWT) res.result();
                EncryptedPushMessage encryptedPushMessage = messageAndJWT.encryptedPushMessage;

                client
                    .postAbs(subscription.getEndpoint())
                    .timeout(connectionTimeoutMillis)
                    .putHeader("Authorization", messageAndJWT.jwt)
                    .putHeader("Content-Type", "application/octet-stream")
                    .putHeader("Content-Encoding", encryptedPushMessage.contentEncoding())
                    .putHeader("TTL", String.valueOf(TTL.hours(1)))
                    .putHeader("Urgency", Urgency.normal())
                    .putHeader("Topic", Topic.ensure("myTopic"))
                    .sendBuffer(Buffer.buffer(encryptedPushMessage.toBytes()))
                    .onSuccess(result -> {
                        requestingCount.decrementAndGet();
                        System.out.println(String.format("status code: %d", result.statusCode()));
                        // 201 Created : Success!
                        // 410 Gone : The subscription is no longer valid.
                        // etc...
                        // for more information, see the useful link below:
                        // [Response from push service - The Web Push Protocol ](https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol)
                    })
                    .onFailure(result -> {
                        requestingCount.decrementAndGet();
                        System.err.println(result);
                    });
            });

            if (currentIndex == targetSubscriptions.size() - 1) {
                return;
            }

            vertx.setTimer(requestIntervalMillis, id -> startInternal(currentIndex + 1));
        }

        private String extractOrigin(PushSubscription subscription) {
            Matcher matcher = URL_ORIGIN_PATTERN.matcher(subscription.getEndpoint());
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                    "This subscription doesn't have origin. " + subscription.getEndpoint());
            }
            return matcher.group(1);
        }

        private static class MessageAndJWT {

            final EncryptedPushMessage encryptedPushMessage;
            final String jwt;

            MessageAndJWT(EncryptedPushMessage encryptedPushMessage, String jwt) {
                this.encryptedPushMessage = encryptedPushMessage;
                this.jwt = jwt;
            }
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
