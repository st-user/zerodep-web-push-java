package com.zerodeplibs.webpush.httpclient;

import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;

/**
 * The "Preparer" used to utilize <a href="https://hc.apache.org/httpcomponents-client-5.1.x/">Apache HTTPClient</a>.
 *
 * <div><b>Thread Safety:</b></div>
 *
 * <p>
 * Instances of this class are immutable. So they can be accessed safely from multiple threads.
 * </p>
 *
 * @author Tomoki Sato
 * @see PreparerBuilder
 */
public class ApacheHttpClientRequestPreparer {

    private final PreparerBuilder.RequestPreparationInfo requestPreparationInfo;

    ApacheHttpClientRequestPreparer(
        PreparerBuilder.RequestPreparationInfo requestPreparationInfo) {
        this.requestPreparationInfo = requestPreparationInfo;
    }

    /**
     * Gets a new {@link PreparerBuilder} used to construct {@link ApacheHttpClientRequestPreparer}.
     *
     * @return a new {@link PreparerBuilder}
     *     used to construct {@link ApacheHttpClientRequestPreparer}.
     */
    public static PreparerBuilder<ApacheHttpClientRequestPreparer> getBuilder() {
        return new ApacheHttpClientRequestPreparer.ApacheHttpClientRequestPreparerBuilder();
    }

    /**
     * Converts this object to an {@link HttpPost}.
     *
     * @return an {@link HttpPost}.
     */
    public HttpPost toHttpPost() {

        HttpPost httpPost = new HttpPost(requestPreparationInfo.getEndpointUrl());
        httpPost.addHeader("Authorization", requestPreparationInfo.getVapidHeader());
        httpPost.addHeader(TTL.HEADER_NAME, requestPreparationInfo.getTtlString());
        httpPost.addHeader(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

        requestPreparationInfo.getEncryptedPushMessage().ifPresent(encryptedPushMessage -> {
            httpPost.setEntity(new ByteArrayEntity(encryptedPushMessage.toBytes(),
                ContentType.create(encryptedPushMessage.mediaType()),
                encryptedPushMessage.contentEncoding()));
        });

        if (!requestPreparationInfo.getEncryptedPushMessage().isPresent()) {
            httpPost.setEntity(
                new ByteArrayEntity(new byte[0], ContentType.APPLICATION_OCTET_STREAM));
        }

        requestPreparationInfo.getTopic().ifPresent(topic -> {
            httpPost.addHeader(Topic.HEADER_NAME, topic);
        });

        return httpPost;
    }

    /**
     * Converts this object to a {@link SimpleHttpRequest}.
     *
     * @return a {@link SimpleHttpRequest}.
     */
    public SimpleHttpRequest toSimpleHttpRequest() {

        SimpleRequestBuilder builder =
            SimpleRequestBuilder.create("POST")
                .setUri(requestPreparationInfo.getEndpointUrl())
                .addHeader("Authorization", requestPreparationInfo.getVapidHeader())
                .addHeader(TTL.HEADER_NAME, requestPreparationInfo.getTtlString())
                .addHeader(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

        requestPreparationInfo.getEncryptedPushMessage().ifPresent(encryptedPushMessage -> {
            builder.addHeader("Content-Encoding", encryptedPushMessage.contentEncoding())
                .setBody(encryptedPushMessage.toBytes(),
                    ContentType.create(encryptedPushMessage.mediaType()));
        });

        if (!requestPreparationInfo.getEncryptedPushMessage().isPresent()) {
            builder.setBody(new byte[0], ContentType.APPLICATION_OCTET_STREAM);
        }

        requestPreparationInfo.getTopic().ifPresent(topic -> {
            builder.addHeader(Topic.HEADER_NAME, topic);
        });

        return builder.build();
    }

    private static class ApacheHttpClientRequestPreparerBuilder
        extends PreparerBuilder<ApacheHttpClientRequestPreparer> {

        @Override
        protected ApacheHttpClientRequestPreparer buildInternal(
            RequestPreparationInfo requestPreparationInfo) {
            return new ApacheHttpClientRequestPreparer(requestPreparationInfo);
        }
    }
}


