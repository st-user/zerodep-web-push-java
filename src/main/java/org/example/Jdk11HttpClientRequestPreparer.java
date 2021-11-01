package org.example;
/*
import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import com.zerodeplibs.webpush.httpclient.PreparerBuilder;
import java.net.URI;
import java.net.http.HttpRequest;


public class Jdk11HttpClientRequestPreparer {

    private final PreparerBuilder.RequestPreparationInfo requestPreparationInfo;

    Jdk11HttpClientRequestPreparer(
        PreparerBuilder.RequestPreparationInfo requestPreparationInfo) {
        this.requestPreparationInfo = requestPreparationInfo;
    }

    public static PreparerBuilder<Jdk11HttpClientRequestPreparer> getBuilder() {
        return new Jdk11HttpClientRequestPreparer.Jdk11HttpRequestPreparerBuilder();
    }

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

        requestPreparationInfo.getTopic().ifPresent(topic -> {
            builder.header(Topic.HEADER_NAME, topic);
        });

        return builder;
    }

    public HttpRequest toRequest() {
        return toRequestBuilder().build();
    }

    private static class Jdk11HttpRequestPreparerBuilder
        extends PreparerBuilder<Jdk11HttpClientRequestPreparer> {

        @Override
        protected Jdk11HttpClientRequestPreparer buildInternal(
            RequestPreparationInfo requestPreparationInfo) {
            return new Jdk11HttpClientRequestPreparer(requestPreparationInfo);
        }
    }
}
*/