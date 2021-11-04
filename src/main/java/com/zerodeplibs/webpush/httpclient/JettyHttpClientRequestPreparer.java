package com.zerodeplibs.webpush.httpclient;

import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.util.function.BiConsumer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;

/**
 * <p>
 * The "Preparer" used to utilize <a href="https://www.eclipse.org/jetty/documentation/jetty-11/programming-guide/index.html#pg-client">Eclipse Jetty Client Libraries</a>.
 * </p>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Objects of this class are immutable. So they can be accessed safely from multiple threads.
 * </p>
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
     * Gets a new {@link PreparerBuilder} used to construct {@link JettyHttpClientRequestPreparer}.
     *
     * @return a new {@link PreparerBuilder}
     *     used to construct {@link JettyHttpClientRequestPreparer}.
     */
    public static PreparerBuilder<JettyHttpClientRequestPreparer> getBuilder() {
        return new JettyHttpClientRequestPreparer.JettyHttpClientRequestPreparerBuilder();
    }

    /**
     * Converts this object to a {@link Request}.
     *
     * @param httpClient an HTTP client object used to create a {@link Request} object.
     * @return a {@link Request}.
     */
    public Request toRequest(HttpClient httpClient) {

        WebPushPreConditions.checkNotNull(httpClient, "httpClient");

        Request request = httpClient.POST(requestPreparationInfo.getEndpointUrl());
        setHttpFields(request::header, request);

        return request;
    }

    private void setHttpFields(BiConsumer<String, String> setHeader, Request request) {

        setHeader.accept("Authorization", requestPreparationInfo.getVapidHeader());
        setHeader.accept(TTL.HEADER_NAME, requestPreparationInfo.getTtlString());
        setHeader.accept(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

        requestPreparationInfo.getEncryptedPushMessage().ifPresent(encryptedPushMessage -> {
            setHeader.accept("Content-Encoding", encryptedPushMessage.contentEncoding());
            request.content(new BytesContentProvider(encryptedPushMessage.mediaType(),
                encryptedPushMessage.toBytes()), encryptedPushMessage.mediaType());
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
