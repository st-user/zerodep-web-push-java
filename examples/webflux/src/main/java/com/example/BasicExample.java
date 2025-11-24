package com.example;

import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.VAPIDKeyPair;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@SpringBootApplication
@RestController
public class BasicExample {

    @Autowired
    private VAPIDKeyPair vapidKeyPair;

    /**
     * # Step 1.
     * Sends the public key to user agents.
     * <p>
     * The user agents create a push subscription with this public key.
     */
    @GetMapping("/getPublicKey")
    public Mono<byte[]> getPublicKey() {
        return Mono.just(vapidKeyPair.extractPublicKeyInUncompressedForm());
    }

    /**
     * # Step 2.
     * Obtains push subscriptions from user agents.
     * <p>
     * The application server(this application) requests the delivery of push messages with these subscriptions.
     */
    @PostMapping("/subscribe")
    public Mono<?> subscribe(@RequestBody PushSubscription subscription) {
        return this.saveSubscriptionToStorage(subscription);
    }

    /**
     * # Step 3.
     * Requests the delivery of push messages.
     * <p>
     * In this example, for simplicity and testability, we use an HTTP endpoint for this purpose.
     * However, in real applications, this feature doesn't have to be provided as an HTTP endpoint.
     */
    @PostMapping("/sendMessage")
    public Mono<ResponseEntity<RequestResultResponse>> sendMessage(
        @RequestBody MyMessage myMessage) {

        String message = myMessage.message() == null ? "" : myMessage.message();
        RequestResultResponse resultResponse = new RequestResultResponse();
        WebClient client = WebClient.builder().build();

        return getSubscriptionsFromStorage()
            // In order to avoid wasting bandwidth,
            // we send HTTP requests at some intervals.
            .delayElements(Duration.ofMillis(100))
            .flatMap(subscription -> {

                return Mono.fromCallable(() -> {

                        // In some circumstances, the JWT creation and the message encryption
                        // may be considered "blocking" operations.
                        //
                        // On the author's environment, the JWT creation takes about 0.7ms
                        // and the message encryption takes about 1.7ms.
                        //
                        // references:
                        //   https://vertx.io/docs/vertx-core/java/#golden_rule
                        //   https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking

                        return SpringWebClientRequestPreparer.getBuilder()
                            .pushSubscription(subscription)
                            .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
                            .vapidJWTSubject("mailto:example@example.com")
                            .pushMessage(message)
                            .ttl(1, TimeUnit.HOURS)
                            .urgencyLow()
                            .topic("MyTopic")
                            .build(vapidKeyPair);

                        // In this example, we send push messages in simple text format.
                        // You can also send them in JSON format as follows:
                        //
                        // ObjectMapper objectMapper = (Create a new one or get from the DI container.)
                        // ....
                        // .pushMessage(objectMapper.writeValueAsBytes(objectForJson))
                        // ....

                    }).onErrorResume(e ->
                        alertAndResume(e).flatMap(r -> {
                            resultResponse.add(r);
                            return Mono.empty();
                        })
                    )
                    .zipWith(Mono.just(subscription)).subscribeOn(Schedulers.boundedElastic());

            })
            .flatMap(t -> {

                SpringWebClientRequestPreparer preparer = t.getT1();
                PushSubscription subscription = t.getT2();

                // 201 Created : Success!
                // 410 Gone : The subscription is no longer valid.
                // etc...
                // for more information, see the useful link below:
                // [Response from push service - The Web Push Protocol ](https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol)

                return preparer.prepare(client).exchangeToMono(response -> {

                        if (response.statusCode().is2xxSuccessful()) {
                            return Mono.just(new RequestResult(true, ""));
                        }

                        if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS ||
                            response.statusCode().is5xxServerError()) {

                            return Mono.error(
                                new DoRetryException(
                                    String.format(
                                        "Receive %s response",
                                        response.statusCode()
                                    ),
                                    subscription
                                )
                            );
                        }

                        if (response.statusCode() == HttpStatus.GONE) {

                            return removeSubscriptionFromStorage(subscription).flatMap(
                                _ignore ->
                                    Mono.error(
                                        new DoNotRetryException(
                                            String.format(
                                                "Receive %s response",
                                                response.statusCode()
                                            ),
                                            subscription
                                        )
                                    )
                            );
                        }


                        return Mono.error(
                            new DoNotRetryException(
                                String.format(
                                    "Receive %s response",
                                    response.statusCode()
                                ),
                                subscription
                            )
                        );

                    })
                    .retryWhen(
                        // TODO Consider 'Retry-After' header field
                        //   - https://datatracker.ietf.org/doc/html/rfc8030#section-8.4
                        //   - https://stackoverflow.com/questions/65744150/spring-webclient-how-to-retry-with-delay-based-on-response-header
                        Retry.backoff(3, Duration.ofMillis(500))
                            .jitter(0.75)
                            .filter(e -> e instanceof DoRetryException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                throw new DoNotRetryException(
                                    "External Service failed to process after max retries",
                                    subscription);
                            })
                    ).onErrorResume(this::alertAndResume);

            })
            .collect(() -> resultResponse, RequestResultResponse::add)
            .map(ResponseEntity::ok);
    }

    /**
     * The example for [Reactor Netty HTTP Client](https://projectreactor.io/docs/netty/release/reference/index.html#http-client)
     */
    @PostMapping("/sendMessageWithReactorNetty")
    public Mono<ResponseEntity<RequestResultResponse>> sendMessageWithReactorNetty(
        @RequestBody MyMessage myMessage) {

        String message = myMessage.message() == null ? "" : myMessage.message();
        RequestResultResponse resultResponse = new RequestResultResponse();
        HttpClient client = HttpClient.create();

        return getSubscriptionsFromStorage()
            // In order to avoid wasting bandwidth,
            // we send HTTP requests at some intervals.
            .delayElements(Duration.ofMillis(100))
            .flatMap(subscription -> {

                return Mono.fromCallable(() -> {

                        // In some circumstances, the JWT creation and the message encryption
                        // may be considered "blocking" operations.
                        //
                        // On the author's environment, the JWT creation takes about 0.7ms
                        // and the message encryption takes about 1.7ms.
                        //
                        // references:
                        //   https://vertx.io/docs/vertx-core/java/#golden_rule
                        //   https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking

                        return ReactorNettyHttpClientRequestPreparer.getBuilder()
                            .pushSubscription(subscription)
                            .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
                            .vapidJWTSubject("mailto:example@example.com")
                            .pushMessage(message)
                            .ttl(1, TimeUnit.HOURS)
                            .urgencyLow()
                            .topic("MyTopic")
                            .build(vapidKeyPair);

                        // In this example, we send push messages in simple text format.
                        // You can also send them in JSON format as follows:
                        //
                        // ObjectMapper objectMapper = (Create a new one or get from the DI container.)
                        // ....
                        // .pushMessage(objectMapper.writeValueAsBytes(objectForJson))
                        // ....

                    }).onErrorResume(e ->
                        alertAndResume(e).flatMap(r -> {
                            resultResponse.add(r);
                            return Mono.empty();
                        })
                    )
                    .zipWith(Mono.just(subscription)).subscribeOn(Schedulers.boundedElastic());

            })
            .flatMap(t -> {

                ReactorNettyHttpClientRequestPreparer preparer = t.getT1();
                PushSubscription subscription = t.getT2();

                // 201 Created : Success!
                // 410 Gone : The subscription is no longer valid.
                // etc...
                // for more information, see the useful link below:
                // [Response from push service - The Web Push Protocol ](https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol)

                return preparer.prepare(client).response().flatMap(response -> {

                        HttpStatus status = HttpStatus.valueOf(response.status().code());

                        if (status.is2xxSuccessful()) {
                            return Mono.just(new RequestResult(true, ""));
                        }

                        if (status == HttpStatus.TOO_MANY_REQUESTS ||
                            status.is5xxServerError()) {

                            return Mono.error(
                                new DoRetryException(
                                    String.format(
                                        "Receive %s response",
                                        status
                                    ),
                                    subscription
                                )
                            );
                        }

                        if (status == HttpStatus.GONE) {

                            return removeSubscriptionFromStorage(subscription).flatMap(
                                _ignore ->
                                    Mono.error(
                                        new DoNotRetryException(
                                            String.format(
                                                "Receive %s response",
                                                status
                                            ),
                                            subscription
                                        )
                                    )
                            );
                        }


                        return Mono.error(
                            new DoNotRetryException(
                                String.format(
                                    "Receive %s response",
                                    status
                                ),
                                subscription
                            )
                        );

                    })
                    .retryWhen(
                        // TODO Consider 'Retry-After' header field
                        //   - https://datatracker.ietf.org/doc/html/rfc8030#section-8.4
                        //   - https://stackoverflow.com/questions/65744150/spring-webclient-how-to-retry-with-delay-based-on-response-header
                        Retry.backoff(3, Duration.ofMillis(500))
                            .jitter(0.75)
                            .filter(e -> e instanceof DoRetryException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                throw new DoNotRetryException(
                                    "External Service failed to process after max retries",
                                    subscription);
                            })
                    ).onErrorResume(this::alertAndResume);

            })
            .collect(() -> resultResponse, RequestResultResponse::add)
            .map(ResponseEntity::ok);
    }

    private Flux<PushSubscription> getSubscriptionsFromStorage() {
        return Flux.fromIterable(
            this.subscriptionMap.values().stream()
                .sorted(Comparator.comparing(PushSubscription::getEndpoint))
                .collect(Collectors.toList())
        );
    }

    private Mono<PushSubscription> saveSubscriptionToStorage(PushSubscription subscription) {
        return Mono.fromCallable(() -> {
            this.subscriptionMap.put(subscription.getEndpoint(), subscription);
            return subscription;
        });
    }

    private Mono<PushSubscription> removeSubscriptionFromStorage(PushSubscription subscription) {
        return Mono.fromCallable(() -> subscriptionMap.remove(subscription.getEndpoint()));
    }

    private Mono<RequestResult> alertAndResume(Throwable e) {
        return sendAlertToMonitoringSystem(e)
            .map(_ignore -> new RequestResult(false, e.getMessage()));
    }

    private Mono<Boolean> sendAlertToMonitoringSystem(Throwable e) {
        return Mono.fromCallable(() -> {
            if (e instanceof RequestException re) {

                logger.error("{} from {}", e.getMessage(), re.shortUrl());

                return true;

            } else {

                logger.error(
                    "Encountered an error whiling requesting the delivery of push notification", e);

                return false;
            }
        });
    }

    private final Logger logger = LoggerFactory.getLogger(BasicExample.class);
    private final Map<String, PushSubscription> subscriptionMap = new ConcurrentHashMap<>();

    public record MyMessage(String message) {
    }

    private static class RequestException extends RuntimeException {

        private static final Pattern SHORT_URL_PATTERN = Pattern.compile("(https?://.*?/.{0,10})");
        private final PushSubscription subscription;

        RequestException(String message, PushSubscription subscription) {
            super(message);
            this.subscription = subscription;
        }

        String shortUrl() {
            Matcher matcher = SHORT_URL_PATTERN.matcher(this.subscription.getEndpoint());
            return matcher.find() ? matcher.group(1) + "..." : "-";
        }
    }

    private static class DoRetryException extends RequestException {
        DoRetryException(String message, PushSubscription subscription) {
            super(message, subscription);
        }
    }

    private static class DoNotRetryException extends RequestException {
        DoNotRetryException(String message, PushSubscription subscription) {
            super(message, subscription);
        }
    }

    record RequestResult(boolean isSuccess, String message) {
    }

    public static class RequestResultResponse {
        private int totalSuccess;
        private int totalFailure;

        synchronized void add(RequestResult r) {
            if (r.isSuccess) {
                this.totalSuccess++;
            } else {
                this.totalFailure++;
            }
        }

        public int getTotalSuccess() {
            return totalSuccess;
        }

        public int getTotalFailure() {
            return totalFailure;
        }

    }
}
