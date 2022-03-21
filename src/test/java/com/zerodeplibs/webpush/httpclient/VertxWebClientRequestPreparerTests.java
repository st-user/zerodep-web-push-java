package com.zerodeplibs.webpush.httpclient;

import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;
import static com.zerodeplibs.webpush.httpclient.PreparerTestUtil.createPushSubscription;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.HttpResponseImpl;
import io.vertx.ext.web.client.impl.WebClientBase;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class VertxWebClientRequestPreparerTests {


    @Test
    public void buildWithSpecifiedProperties()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        String endpoint = "https://example.com/test";
        String subject = "mailto:example@example.com";
        Instant expirationTime = Instant.now();

        PushSubscription pushSubscription = createPushSubscription(endpoint);

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(VAPIDJWTParam.getBuilder()
                .resourceURLString(endpoint)
                .expirationTime(expirationTime)
                .subject(subject)
                .additionalClaim("claimName", "claimValue")
                .build()
            );

        Vertx vertx = Vertx.vertx();
        TestingWebClient webClient = new TestingWebClient(vertx.createHttpClient());

        Future<HttpResponse<Buffer>> httpResponseFuture = VertxWebClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .vapidJWTExpirationTime(expirationTime)
            .vapidJWTSubject(subject)
            .vapidJWTAdditionalClaim("claimName", "claimValue")
            .pushMessage("Hello World")
            .ttl(1, TimeUnit.HOURS)
            .urgencyHigh()
            .topic("MyTopic")
            .build(vapidKeyPair)
            .sendBuffer(webClient, req -> {
                req.timeout(1234);
            });

        assertThat(webClient.request.url, equalTo("https://example.com/test"));
        assertThat(webClient.request.headers.get("Authorization"), equalTo("vapid for test"));
        assertThat(webClient.request.headers.get("Content-Type"),
            equalToIgnoringCase("application/octet-stream"));
        assertThat(webClient.request.headers.get("Content-Encoding"), equalTo("aes128gcm"));
        assertThat(webClient.request.headers.get("TTL"), equalTo("3600"));
        assertThat(webClient.request.headers.get("Urgency"), equalTo("high"));
        assertThat(webClient.request.headers.get("Topic"), equalTo("MyTopic"));
        assertThat(webClient.request.body.getBytes().length, greaterThan(0));

        assertThat(webClient.request.timeout, equalTo(1234L));

        HttpResponse<Buffer> result = httpResponseFuture.result();
        assertThat(result.statusCode(), equalTo(200));
        assertThat(result.statusMessage(), equalTo("a push service response"));
    }

    @Test
    public void buildWithDefaultValue()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {


        String endpoint = "https://example.com/test";

        PushSubscription pushSubscription = createPushSubscription(endpoint);

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(null);

        Vertx vertx = Vertx.vertx();
        TestingWebClient webClient = new TestingWebClient(vertx.createHttpClient());

        Future<HttpResponse<Buffer>> httpResponseFuture = VertxWebClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .build(vapidKeyPair)
            .sendBuffer(webClient, req -> {
            });

        assertThat(webClient.request.url, equalTo("https://example.com/test"));
        assertThat(webClient.request.headers.get("Authorization"), equalTo("vapid for test"));
        assertThat(webClient.request.headers.get("Content-Type"), is(nullValue()));
        assertThat(webClient.request.headers.get("Content-Encoding"), is(nullValue()));
        assertThat(webClient.request.headers.get("TTL"), equalTo(String.valueOf(24 * 60 * 60)));
        assertThat(webClient.request.headers.get("Urgency"), equalTo("normal"));
        assertThat(webClient.request.headers.get("Topic"), is(nullValue()));
        assertThat(webClient.request.body.length(), equalTo(0));

        HttpResponse<Buffer> result = httpResponseFuture.result();
        assertThat(result.statusCode(), equalTo(200));
        assertThat(result.statusMessage(), equalTo("a push service response"));
    }

    @Test
    public void shouldThrowExceptionWhenIllegalObjectsArePassedToTheArgumentsOfSendBuffer()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        PushSubscription pushSubscription = createPushSubscription("https://example.com/test");

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(null);

        VertxWebClientRequestPreparer preparer = VertxWebClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .build(vapidKeyPair);

        assertNullCheck(() -> preparer.sendBuffer(null, null, null),
            "webClient");

        Vertx vertx = Vertx.vertx();
        TestingWebClient webClient = new TestingWebClient(vertx.createHttpClient());

        assertNullCheck(() -> preparer.sendBuffer(webClient, null, null),
            "requestConsumer");

        assertNullCheck(() -> preparer.sendBuffer(webClient, r -> {
        }, null), "handler");
    }

    private static class TestingWebClient extends WebClientBase {

        TestingHttpRequest request;

        TestingWebClient(HttpClient client) {
            super(client, new WebClientOptions());
        }

        @Override
        public HttpRequest<Buffer> postAbs(String absoluteURI) {
            this.request = new TestingHttpRequest(absoluteURI);
            return this.request;
        }
    }

    private static class TestingHttpRequest implements HttpRequest<Buffer> {

        final String url;
        final Map<String, String> headers = new HashMap<>();
        long timeout;
        Buffer body;

        TestingHttpRequest(String url) {
            this.url = url;
        }

        @Override
        public HttpRequest<Buffer> method(HttpMethod value) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> port(int value) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public <U> HttpRequest<U> as(BodyCodec<U> responseCodec) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> host(String value) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> virtualHost(String value) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> uri(String value) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> putHeaders(MultiMap headers) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> putHeader(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        @Override
        public HttpRequest<Buffer> putHeader(String name, Iterable<String> value) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public MultiMap headers() {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> authentication(Credentials credentials) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> ssl(Boolean value) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> timeout(long value) {
            this.timeout = value;
            return this;
        }

        @Override
        public HttpRequest<Buffer> addQueryParam(String paramName, String paramValue) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> setQueryParam(String paramName, String paramValue) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> followRedirects(boolean value) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> proxy(ProxyOptions proxyOptions) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> expect(ResponsePredicate predicate) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public MultiMap queryParams() {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> copy() {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public HttpRequest<Buffer> multipartMixed(boolean allow) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public void sendStream(ReadStream<Buffer> body,
                               Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public void sendBuffer(Buffer body,
                               Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
            this.body = body;

            handler.handle(new AsyncResult<HttpResponse<Buffer>>() {


                @Override
                public HttpResponse<Buffer> result() {
                    return new HttpResponseImpl<>(
                        HttpVersion.HTTP_1_1, 200,
                        "a push service response", null, null, null, null, null
                    );
                }

                @Override
                public Throwable cause() {
                    return null;
                }

                @Override
                public boolean succeeded() {
                    return true;
                }

                @Override
                public boolean failed() {
                    return false;
                }
            });
        }

        @Override
        public void sendJsonObject(JsonObject body,
                                   Handler<AsyncResult<HttpResponse<Buffer>>> handler) {

            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public void sendJson(Object body,
                             Handler<AsyncResult<HttpResponse<Buffer>>> handler) {

            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public void sendForm(MultiMap body,
                             Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        public void sendForm(MultiMap body, String charset,
                             Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public void sendMultipartForm(MultipartForm body,
                                      Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
            throw new UnsupportedOperationException("Unexpected invocation.");
        }

        @Override
        public void send(
            Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
            this.sendBuffer(null, handler);
        }
    }
}
