package com.zerodeplibs.webpush.httpclient;

import com.zerodeplibs.webpush.EncryptedPushMessage;
import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The "Preparer" used to utilize <a href="https://vertx.io/docs/vertx-web-client/java/">Vert.x Web Client</a>.
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
public class VertxWebClientRequestPreparer {


    private final PreparerBuilder.RequestPreparationInfo requestPreparationInfo;

    VertxWebClientRequestPreparer(
        PreparerBuilder.RequestPreparationInfo requestPreparationInfo) {
        this.requestPreparationInfo = requestPreparationInfo;
    }

    /**
     * Send a request with the given <code>webClient</code>.
     * The <code>handler</code> will receive the response as an {@link HttpResponse}.
     *
     * <p>
     * In order to construct the request object with arbitrary parameters,
     * pass a consumer to the <code>requestConsumer</code> argument like:
     * </p>
     * <pre class="code">
     * preparer.sendBuffer(
     *     webClient,
     *     request -&gt; {
     *         request.timeout(10_000L)
     *     },
     *     response -&gt; { ... }
     * )
     * </pre>
     *
     * @param webClient       the web client used to create an HTTP request.
     * @param requestConsumer the consumer used to set arbitrary parameters.
     * @param handler         the handler receiving the response.
     * @see HttpRequest#sendBuffer(Buffer, Handler)
     */
    public void sendBuffer(WebClient webClient,
                           Consumer<HttpRequest<Buffer>> requestConsumer,
                           Handler<AsyncResult<HttpResponse<Buffer>>> handler) {

        WebPushPreConditions.checkNotNull(webClient, "webClient");
        WebPushPreConditions.checkNotNull(requestConsumer, "requestConsumer");
        WebPushPreConditions.checkNotNull(handler, "handler");

        HttpRequest<Buffer> request = webClient.postAbs(requestPreparationInfo.getEndpointUrl());

        request.putHeader("Authorization", requestPreparationInfo.getVapidHeader());
        request.putHeader(TTL.HEADER_NAME, requestPreparationInfo.getTtlString());
        request.putHeader(Urgency.HEADER_NAME, requestPreparationInfo.getUrgency());

        Optional<EncryptedPushMessage> encryptedPushMessage =
            requestPreparationInfo.getEncryptedPushMessage();

        if (encryptedPushMessage.isPresent()) {
            request.putHeader("Content-Type", encryptedPushMessage.get().mediaType());
            request.putHeader("Content-Encoding", encryptedPushMessage.get().contentEncoding());
        }

        requestPreparationInfo.getTopic().ifPresent(topic -> {
            request.putHeader(Topic.HEADER_NAME, topic);
        });

        requestConsumer.accept(request);

        if (encryptedPushMessage.isPresent()) {
            request.sendBuffer(Buffer.buffer(encryptedPushMessage.get().toBytes()), handler);
        } else {
            request.sendBuffer(Buffer.buffer(), handler);
        }
    }

    /**
     * Send a request with the given <code>webClient</code>.
     * If the request succeeds, the response is available as an {@link HttpResponse}
     * through the returned {@link Future}.
     *
     * <p>
     * In order to construct the request object with arbitrary parameters,
     * pass a consumer to the <code>requestConsumer</code> argument like:
     * </p>
     * <pre class="code">
     * preparer.sendBuffer(
     *     webClient,
     *     request -&gt; {
     *         request.timeout(10_000L)
     *     }
     * )
     * </pre>
     *
     * @param webClient       the web client used to create an HTTP request.
     * @param requestConsumer the consumer used to set arbitrary parameters.
     * @return the future result of the request.
     */
    public Future<HttpResponse<Buffer>> sendBuffer(WebClient webClient,
                                                   Consumer<HttpRequest<Buffer>> requestConsumer) {
        Promise<HttpResponse<Buffer>> promise = Promise.promise();
        sendBuffer(webClient, requestConsumer, promise);
        return promise.future();
    }

    /**
     * Gets a new {@link PreparerBuilder} used to construct {@link VertxWebClientRequestPreparer}.
     *
     * @return a new {@link PreparerBuilder}
     *     used to construct {@link VertxWebClientRequestPreparer}.
     */
    public static PreparerBuilder<VertxWebClientRequestPreparer> getBuilder() {
        return new VertxWebClientRequestPreparer.VertxWebClientRequestPreparerBuilder();
    }

    private static class VertxWebClientRequestPreparerBuilder
        extends PreparerBuilder<VertxWebClientRequestPreparer> {

        @Override
        protected VertxWebClientRequestPreparer buildInternal(
            RequestPreparationInfo requestPreparationInfo) {
            return new VertxWebClientRequestPreparer(requestPreparationInfo);
        }
    }


}
