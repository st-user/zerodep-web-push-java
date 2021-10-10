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
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
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
import java.util.stream.Collectors;

public class Example {

    private static VAPIDKeyPair createVAPIDKeyPair(Vertx vertx) throws IOException {
        return VAPIDKeyPairs.of(
            PrivateKeySources.ofPEMFile(new File("./.keys/my-private_pkcs8.pem").toPath()),
            PublicKeySources.ofPEMFile(new File("./.keys/my-pub.pem").toPath()),
            (privateKey, publicKey) -> new MyVertxVAPIDJWTGenerator(vertx, privateKey));
    }

    public static void main(String[] args) throws IOException {

        Vertx vertx = Vertx.vertx();
        WebClient client = WebClient.create(vertx);
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        VAPIDKeyPair vapidKeyPair = createVAPIDKeyPair(vertx);
        MockSubscriptionStorage mockStorage = new MockSubscriptionStorage();

        router
            .get("/getPublicKey")
            .respond(
                ctx -> ctx
                    .response()
                    .putHeader("Content-Type", "application/octet-stream")
                    .end(Buffer.buffer(vapidKeyPair.extractPublicKeyInUncompressedForm()))
            );

        router
            .post("/subscribe")
            .handler(ctx -> {

                PushSubscription subscription =
                    ctx.getBodyAsJson().mapTo(PushSubscription.class);
                mockStorage.saveSubscriptionToStorage(subscription);

                ctx.response().end();
            });

        router
            .post("/sendMessage")
            .handler(ctx -> {

                String message = ctx.getBodyAsJson().getString("message");
                message = message == null ? "" : message.trim();
                message = message.length() == 0 ? "Default Message." : message;

                vertx.getOrCreateContext().put("messageToSend", message);

                new RequestWorker(
                    vertx, client, vapidKeyPair, mockStorage.getSubscriptionsFromStorage()
                ).start();

                ctx.response()
                    .putHeader("Content-Type", "text/plain")
                    .end("Started sending notifications.");
            });

        router.route("/*").handler(StaticHandler.create());

        server.requestHandler(router).listen(8080, res -> {
            System.out.println("Vert.x HTTP server started.");
        });
    }

    static class RequestWorker {

        private final Vertx vertx;
        private final WebClient client;
        private final VAPIDKeyPair vapidKeyPair;
        private final List<PushSubscription> targetSubscriptions;

        private final AtomicLong requestingCount;

        private final int requestIntervalMillis;
        private final int maxConcurrency;
        private final int retryTimerMillis;

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
            this.maxConcurrency = 5;
            this.retryTimerMillis = 500;

            this.requestingCount = new AtomicLong();
        }

        void start() {
            startInternal(0);
        }

        private void startInternal(int currentIndex) {

            if (requestingCount.intValue() >= maxConcurrency) {
                vertx.setTimer(retryTimerMillis, id -> startInternal(currentIndex));
                return;
            }
            String message = vertx.getOrCreateContext().get("messageToSend");
            PushSubscription subscription = targetSubscriptions.get(currentIndex);

            vertx.executeBlocking(promise -> {

                MessageEncryption messageEncryption = MessageEncryptions.of();
                EncryptedPushMessage encryptedPushMessage = messageEncryption.encrypt(
                    UserAgentMessageEncryptionKeyInfo.from(subscription.getKeys()),
                    PushMessage.ofUTF8(message)
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
                    .putHeader("Authorization", messageAndJWT.jwt)
                    .putHeader("Content-Type", "application/octet-stream")
                    .putHeader("Content-Encoding", encryptedPushMessage.contentEncoding())
                    .putHeader("TTL", String.valueOf(TTL.hours(1)))
                    .putHeader("Urgency", Urgency.normal())
                    .putHeader("Topic", Topic.ensure("myTopic"))
                    .sendBuffer(Buffer.buffer(encryptedPushMessage.toBytes()))
                    .onSuccess(result -> {
                        requestingCount.decrementAndGet();
                        System.out.println(String.format("Success! code: %d", result.statusCode()));
                    })
                    .onFailure(result -> {
                        requestingCount.decrementAndGet();
                        System.err.println(result);
                    });
            });

/*
            Future.succeededFuture()
                .compose(_o -> {

                    PushMessage pushMessage = PushMessage.ofUTF8(message);

                    MessageEncryption messageEncryption = MessageEncryptions.of();
                    EncryptedPushMessage encryptedPushMessage = messageEncryption.encrypt(
                        UserAgentMessageEncryptionKeyInfo.from(subscription.getKeys()),
                        pushMessage
                    );

                    return Future.succeededFuture(encryptedPushMessage);

                }).compose(encryptedPushMessage -> {

                    VAPIDJWTParam vapidjwtParam = VAPIDJWTParam.getBuilder()
                        .resourceURLString(subscription.getEndpoint())
                        .expiresAfterSeconds((int) TimeUnit.MINUTES.toSeconds(15))
                        .subject("mailto:example@example.com")
                        .build();

                    requestingCount.incrementAndGet();

                    return client
                        .postAbs(subscription.getEndpoint())
                        .putHeader("Authorization",
                            vapidKeyPair.generateAuthorizationHeaderValue(vapidjwtParam))
                        .putHeader("Content-Type", "application/octet-stream")
                        .putHeader("Content-Encoding", encryptedPushMessage.contentEncoding())
                        .putHeader("TTL", String.valueOf(TTL.hours(1)))
                        .putHeader("Urgency", Urgency.normal())
                        .putHeader("Topic", Topic.ensure("myTopic"))
                        .sendBuffer(Buffer.buffer(encryptedPushMessage.toBytes()));

                })
                .onSuccess(result -> {

                    requestingCount.decrementAndGet();
                    System.out.println(String.format("Success! code: %d", result.statusCode()));

                })
                .onFailure(result -> {

                    requestingCount.decrementAndGet();
                    System.err.println(result);

                });

*/

            if (currentIndex == targetSubscriptions.size() - 1) {
                return;
            }

            vertx.setTimer(requestIntervalMillis, id -> startInternal(currentIndex + 1));
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
}
