package com.zerodeplibs.webpush.httpclient;

import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * The "Preparer" for utilizing <a href="https://square.github.io/okhttp/">OkHttp</a>.
 *
 * @see PreparerBuilder
 * @author Tomoki Sato
 */
public class OkHttpClientRequestPreparer {

    private final PreparerBuilder.RequestPreparationInfo requestPreparationInfo;

    OkHttpClientRequestPreparer(
        PreparerBuilder.RequestPreparationInfo requestPreparationInfo) {
        this.requestPreparationInfo = requestPreparationInfo;
    }

    /**
     * Gets a new {@link PreparerBuilder} for {@link OkHttpClientRequestPreparer}.
     *
     * @return a new {@link PreparerBuilder} for {@link OkHttpClientRequestPreparer}.
     */
    public static PreparerBuilder<OkHttpClientRequestPreparer> getBuilder() {
        return new OkHttpRequestPreparerBuilder();
    }

    /**
     * Converts this object to a {@link Request.Builder}.
     *
     * @return a {@link Request.Builder}.
     */
    public Request.Builder toRequestBuilder() {


        Request.Builder builder = new Request.Builder()
            .url(requestPreparationInfo.getEndpointUrl())
            .addHeader("Authorization", requestPreparationInfo.getVapidHeader())
            .addHeader(TTL.HEADER_NAME, requestPreparationInfo.getTtlString())
            .addHeader(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

        requestPreparationInfo.getEncryptedPushMessage().ifPresent(encryptedPushMessage -> {
            builder.addHeader("Content-Type", encryptedPushMessage.mediaType())
                .addHeader("Content-Encoding", encryptedPushMessage.contentEncoding())
                .post(RequestBody.create(encryptedPushMessage.toBytes()));
        });


        requestPreparationInfo.getTopic().ifPresent(topic -> {
            builder.addHeader(Topic.HEADER_NAME, topic);
        });

        return builder;
    }

    /**
     * Converts this object to a {@link Request}.
     *
     * @return a {@link Request}.
     */
    public Request toRequest() {
        return toRequestBuilder().build();
    }

    private static class OkHttpRequestPreparerBuilder
        extends PreparerBuilder<OkHttpClientRequestPreparer> {

        @Override
        protected OkHttpClientRequestPreparer buildInternal(
            RequestPreparationInfo requestPreparationInfo) {
            return new OkHttpClientRequestPreparer(requestPreparationInfo);
        }
    }
}
