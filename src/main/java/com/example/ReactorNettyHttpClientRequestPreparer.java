package com.example;

import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import com.zerodeplibs.webpush.httpclient.PreparerBuilder;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

public class ReactorNettyHttpClientRequestPreparer {

    private final PreparerBuilder.RequestPreparationInfo requestPreparationInfo;

    ReactorNettyHttpClientRequestPreparer(
        PreparerBuilder.RequestPreparationInfo requestPreparationInfo) {
        this.requestPreparationInfo = requestPreparationInfo;
    }

    /**
     * Gets a new {@link PreparerBuilder} used
     * to construct {@link ReactorNettyHttpClientRequestPreparer}.
     *
     * @return a new {@link PreparerBuilder}
     *     used to construct {@link ReactorNettyHttpClientRequestPreparer}.
     */
    public static PreparerBuilder<ReactorNettyHttpClientRequestPreparer> getBuilder() {
        return new ReactorNettyHttpClientRequestPreparer.ReactorNettyHttpClientRequestPreparerBuilder();
    }

    public HttpClient.ResponseReceiver<?> prepare(HttpClient httpClient) {

        byte[] body = requestPreparationInfo.getEncryptedPushMessage().map(m -> m.toBytes())
            .orElse(new byte[0]);

        return httpClient
            .headers(headers -> {

                headers
                    .set("Authorization", requestPreparationInfo.getVapidHeader())
                    .set(TTL.HEADER_NAME, requestPreparationInfo.getTtlString())
                    .set(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

                requestPreparationInfo.getTopic().ifPresent(topic -> {
                    headers.set(Topic.HEADER_NAME, topic);
                });

                requestPreparationInfo.getEncryptedPushMessage().ifPresent(encryptedPushMessage -> {
                    headers
                        .set("Content-Type", encryptedPushMessage.mediaType())
                        .set("Content-Encoding", encryptedPushMessage.contentEncoding())
                        .set("Content-Length", encryptedPushMessage.length());
                });

                if (!requestPreparationInfo.getEncryptedPushMessage().isPresent()) {
                    headers.set("Content-Length", 0);
                }


            })
            .post()
            .uri(requestPreparationInfo.getEndpointUrl())
            .send(ByteBufFlux.fromInbound(Flux.just(body)));
    }

    private static class ReactorNettyHttpClientRequestPreparerBuilder
        extends PreparerBuilder<ReactorNettyHttpClientRequestPreparer> {

        @Override
        protected ReactorNettyHttpClientRequestPreparer buildInternal(
            RequestPreparationInfo requestPreparationInfo) {
            return new ReactorNettyHttpClientRequestPreparer(requestPreparationInfo);
        }
    }

}
