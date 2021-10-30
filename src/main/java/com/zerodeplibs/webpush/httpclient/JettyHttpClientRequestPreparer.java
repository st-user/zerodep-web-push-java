package com.zerodeplibs.webpush.httpclient;

import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import java.util.function.BiConsumer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesRequestContent;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;

/**
 * The "Preparer" for utilizing <a href="https://www.eclipse.org/jetty/documentation/jetty-11/programming-guide/index.html#pg-client">Eclipse Jetty Client Libraries</a>.
 *
 * @author Tomoki Sato
 * @see PreparerBuilder
 */
public class JettyHttpClientRequestPreparer {

    private final PreparerBuilder.RequestPreparationInfo requestPreparationInfo;

    JettyHttpClientRequestPreparer(
        PreparerBuilder.RequestPreparationInfo requestPreparationInfo) {
        this.requestPreparationInfo = requestPreparationInfo;
    }

    /**
     * Gets a new {@link PreparerBuilder} for {@link JettyHttpClientRequestPreparer}.
     *
     * @return a new {@link PreparerBuilder} for {@link JettyHttpClientRequestPreparer}.
     */
    public static PreparerBuilder<JettyHttpClientRequestPreparer> getBuilder() {
        return new JettyHttpClientRequestPreparer.JettyHttpClientRequestPreparerBuilder();
    }

    /**
     * Converts this object to a {@link Request}.
     *
     * @return a {@link Request}.
     */
    public Request toRequest(HttpClient httpClient) {

        Request request = httpClient.POST(requestPreparationInfo.getEndpointUrl());
        if (request instanceof HttpRequest) {

            setHttpFields((name, value) -> {
                HttpRequest httpReq = (HttpRequest) request;
                httpReq.addHeader(new HttpField(name, value));
            }, request);

        } else {
            setHttpFields(request::header, request);
        }

        return request;
    }

    private void setHttpFields(BiConsumer<String, String> setHeader, Request request) {

        setHeader.accept(HttpHeader.AUTHORIZATION.asString(),
            requestPreparationInfo.getVapidHeader());
        setHeader.accept(TTL.HEADER_NAME, requestPreparationInfo.getTtlString());
        setHeader.accept(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

        requestPreparationInfo.getEncryptedPushMessage().ifPresent(encryptedPushMessage -> {
            setHeader.accept(HttpHeader.CONTENT_ENCODING.asString(),
                encryptedPushMessage.contentEncoding());
            request.body(new BytesRequestContent(encryptedPushMessage.mediaType(),
                encryptedPushMessage.toBytes()));
        });

        requestPreparationInfo.getTopic().ifPresent(topic -> {
            setHeader.accept(Topic.HEADER_NAME, topic);
        });
    }

    private static class JettyHttpClientRequestPreparerBuilder
        extends PreparerBuilder<JettyHttpClientRequestPreparer> {

        @Override
        protected JettyHttpClientRequestPreparer buildInternal(
            RequestPreparationInfo requestPreparationInfo) {
            return new JettyHttpClientRequestPreparer(requestPreparationInfo);
        }
    }
}
