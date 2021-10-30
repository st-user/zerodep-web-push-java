package com.zerodeplibs.webpush.httpclient;

import static com.zerodeplibs.webpush.httpclient.PreparerTestUtil.createPushSubscription;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ProtocolException;
import org.junit.jupiter.api.Test;

public class ApacheHttpClientRequestPreparerTests {

    @Test
    public void buildHttpPostWithSpecifiedProperties()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, ProtocolException,
        URISyntaxException, MalformedURLException {

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

        HttpPost httpPost = ApacheHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .vapidJWTExpirationTime(expirationTime)
            .vapidJWTSubject(subject)
            .vapidJWTAdditionalClaim("claimName", "claimValue")
            .pushMessage("Hello World")
            .ttl(1, TimeUnit.HOURS)
            .urgencyHigh()
            .topic("MyTopic")
            .build(vapidKeyPair)
            .toHttpPost();


        assertThat(httpPost.getUri().toURL(), equalTo(new URL("https://example.com/test")));
        assertThat(httpPost.getHeader("Authorization").getValue(), equalTo("vapid for test"));
        assertThat(httpPost.getHeader("TTL").getValue(), equalTo("3600"));
        assertThat(httpPost.getHeader("Urgency").getValue(), equalTo("high"));
        assertThat(httpPost.getHeader("Topic").getValue(), equalTo("MyTopic"));
        assertThat(httpPost.getEntity().getContentType(),
            equalToIgnoringCase("Application/octet-stream"));
        assertThat(httpPost.getEntity().getContentEncoding(), equalTo("aes128gcm"));
        assertThat(httpPost.getEntity().getContentLength(), greaterThan(0L));
    }

    @Test
    public void buildHttpPostWithDefaultValue()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, MalformedURLException,
        ProtocolException, URISyntaxException {


        String endpoint = "https://example.com/test";

        PushSubscription pushSubscription = createPushSubscription(endpoint);

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(null);

        HttpPost httpPost = ApacheHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .build(vapidKeyPair)
            .toHttpPost();

        assertThat(httpPost.getUri().toURL(), equalTo(new URL("https://example.com/test")));
        assertThat(httpPost.getHeader("Authorization").getValue(), equalTo("vapid for test"));
        assertThat(httpPost.getHeader("Content-Type"), is(nullValue()));
        assertThat(httpPost.getHeader("Content-Encoding"), is(nullValue()));
        assertThat(httpPost.getHeader("TTL").getValue(), equalTo(String.valueOf(24 * 60 * 60)));
        assertThat(httpPost.getHeader("Urgency").getValue(), equalTo("normal"));
        assertThat(httpPost.getHeader("Topic"), is(nullValue()));
        assertThat(httpPost.getEntity(), is(nullValue()));
    }

    @Test
    public void buildSimpleHttpRequestWithSpecifiedProperties()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, ProtocolException,
        URISyntaxException, MalformedURLException {

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

        SimpleHttpRequest request = ApacheHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .vapidJWTExpirationTime(expirationTime)
            .vapidJWTSubject(subject)
            .vapidJWTAdditionalClaim("claimName", "claimValue")
            .pushMessage("Hello World")
            .ttl(1, TimeUnit.HOURS)
            .urgencyHigh()
            .topic("MyTopic")
            .build(vapidKeyPair)
            .toSimpleHttpRequest();

        assertThat(request.getUri().toURL(), equalTo(new URL("https://example.com/test")));
        assertThat(request.getHeader("Authorization").getValue(), equalTo("vapid for test"));
        assertThat(request.getHeader("Content-Encoding").getValue(), equalTo("aes128gcm"));
        assertThat(request.getHeader("TTL").getValue(), equalTo("3600"));
        assertThat(request.getHeader("Urgency").getValue(), equalTo("high"));
        assertThat(request.getHeader("Topic").getValue(), equalTo("MyTopic"));
        assertThat(request.getBody().getContentType().getMimeType(),
            equalTo(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));
        assertThat(request.getBody().getBodyBytes().length, greaterThan(0));
    }

    @Test
    public void buildSimpleHttpRequestWithDefaultValue()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, URISyntaxException,
        MalformedURLException, ProtocolException {


        String endpoint = "https://example.com/test";

        PushSubscription pushSubscription = createPushSubscription(endpoint);

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(null);

        SimpleHttpRequest request = ApacheHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .build(vapidKeyPair)
            .toSimpleHttpRequest();

        assertThat(request.getUri().toURL(), equalTo(new URL("https://example.com/test")));
        assertThat(request.getHeader("Authorization").getValue(), equalTo("vapid for test"));
        assertThat(request.getHeader("Content-Type"), is(nullValue()));
        assertThat(request.getHeader("Content-Encoding"), is(nullValue()));
        assertThat(request.getHeader("TTL").getValue(), equalTo(String.valueOf(24 * 60 * 60)));
        assertThat(request.getHeader("Urgency").getValue(), equalTo("normal"));
        assertThat(request.getHeader("Topic"), is(nullValue()));
        assertThat(request.getBody(), is(nullValue()));
    }

}
