package com.zerodeplibs.webpush.httpclient;

import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import java.net.URI;
import java.net.http.HttpRequest;

/**
 * <p>
 * The "Preparer" used to utilize {@link java.net.http.HttpClient}.
 * </p>
 *
 * <div><b>Example:</b></div>
 * <pre class="code">
 * VAPIDKeyPair vapidKeyPair = .....
 *
 * HttpRequest request = StandardHttpClientRequestPreparer.getBuilder()
 *     .pushSubscription(subscription)
 *     .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
 *     .vapidJWTSubject("mailto:example@example.com")
 *     .pushMessage(message)
 *     .ttl(1, TimeUnit.HOURS)
 *     .urgencyLow()
 *     .topic("MyTopic")
 *     .build(vapidKeyPair)
 *     .toRequest();
 *
 * </pre>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Instances of this class are immutable. So they can be accessed safely from multiple threads.
 * </p>
 *
 * @author Tomoki Sato
 * @see PreparerBuilder
 */
public class StandardHttpClientRequestPreparer {

    private final PreparerBuilder.RequestPreparationInfo requestPreparationInfo;

    StandardHttpClientRequestPreparer(
        PreparerBuilder.RequestPreparationInfo requestPreparationInfo) {
        this.requestPreparationInfo = requestPreparationInfo;
    }

    /**
     * Gets a new {@link PreparerBuilder} used
     * to construct {@link StandardHttpClientRequestPreparer}.
     *
     * @return a new {@link PreparerBuilder}
     *     used to construct {@link StandardHttpClientRequestPreparer}.
     */
    public static PreparerBuilder<StandardHttpClientRequestPreparer> getBuilder() {
        return new StandardHttpClientRequestPreparer.StandardHttpRequestPreparerBuilder();
    }

    /**
     * Converts this object to a {@link HttpRequest.Builder}.
     *
     * @return a {@link HttpRequest.Builder}.
     */
    public HttpRequest.Builder toRequestBuilder() {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(requestPreparationInfo.getEndpointUrl()))
            .header("Authorization", requestPreparationInfo.getVapidHeader())
            .header(TTL.HEADER_NAME, requestPreparationInfo.getTtlString())
            .header(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

        requestPreparationInfo.getEncryptedPushMessage().ifPresent(encryptedPushMessage -> {
            builder.header("Content-Type", encryptedPushMessage.mediaType())
                .header("Content-Encoding", encryptedPushMessage.contentEncoding())
                .POST(HttpRequest.BodyPublishers.ofByteArray(encryptedPushMessage.toBytes()));
        });

        if (!requestPreparationInfo.getEncryptedPushMessage().isPresent()) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        }

        requestPreparationInfo.getTopic().ifPresent(topic -> {
            builder.header(Topic.HEADER_NAME, topic);
        });

        return builder;
    }

    /**
     * Converts this object to a {@link HttpRequest}.
     *
     * @return a {@link HttpRequest}.
     */
    public HttpRequest toRequest() {
        return toRequestBuilder().build();
    }

    private static class StandardHttpRequestPreparerBuilder
        extends PreparerBuilder<StandardHttpClientRequestPreparer> {

        @Override
        protected StandardHttpClientRequestPreparer buildInternal(
            RequestPreparationInfo requestPreparationInfo) {
            return new StandardHttpClientRequestPreparer(requestPreparationInfo);
        }
    }

}
