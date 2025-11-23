package com.example;

import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import com.zerodeplibs.webpush.httpclient.PreparerBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class SpringWebClientRequestPreparer {

    private final PreparerBuilder.RequestPreparationInfo requestPreparationInfo;

    SpringWebClientRequestPreparer(
        PreparerBuilder.RequestPreparationInfo requestPreparationInfo) {
        this.requestPreparationInfo = requestPreparationInfo;
    }

    /**
     * Gets a new {@link PreparerBuilder} used
     * to construct {@link SpringWebClientRequestPreparer}.
     *
     * @return a new {@link PreparerBuilder}
     *     used to construct {@link SpringWebClientRequestPreparer}.
     */
    public static PreparerBuilder<SpringWebClientRequestPreparer> getBuilder() {
        return new SpringWebClientRequestPreparer.SpringWebClientRequestPreparerBuilder();
    }

    public WebClient.RequestHeadersSpec<?> prepare(WebClient client) {

        WebClient.RequestBodySpec spec = client.method(HttpMethod.POST)
            .uri(requestPreparationInfo.getEndpointUrl())
            .header("Authorization", requestPreparationInfo.getVapidHeader())
            .header(TTL.HEADER_NAME, requestPreparationInfo.getTtlString())
            .header(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

        requestPreparationInfo.getTopic().ifPresent(topic -> {
            spec.header(Topic.HEADER_NAME, topic);
        });

        requestPreparationInfo.getEncryptedPushMessage().ifPresent(encryptedPushMessage -> {
            spec.header("Content-Type", encryptedPushMessage.mediaType())
                .header("Content-Encoding", encryptedPushMessage.contentEncoding());
        });

        byte[] body = requestPreparationInfo.getEncryptedPushMessage().isPresent() ?
            requestPreparationInfo.getEncryptedPushMessage().get()
                .toBytes() : new byte[0];

        return spec.body(BodyInserters.fromValue(body));
    }

    private static class SpringWebClientRequestPreparerBuilder
        extends PreparerBuilder<SpringWebClientRequestPreparer> {

        @Override
        protected SpringWebClientRequestPreparer buildInternal(
            RequestPreparationInfo requestPreparationInfo) {
            return new SpringWebClientRequestPreparer(requestPreparationInfo);
        }
    }

}
