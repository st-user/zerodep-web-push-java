package com.zerodeplibs.webpush.httpclient;

import static com.zerodeplibs.webpush.httpclient.PreparerTestUtil.createPushSubscription;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class StandardHttpClientRequestPreparerTests {

    @Test
    public void buildWithSpecifiedProperties()
        throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {

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

        HttpRequest request = StandardHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .vapidJWTExpirationTime(expirationTime)
            .vapidJWTSubject(subject)
            .vapidJWTAdditionalClaim("claimName", "claimValue")
            .pushMessage("Hello World")
            .ttl(1, TimeUnit.HOURS)
            .urgencyHigh()
            .topic("MyTopic")
            .build(vapidKeyPair)
            .toRequest();

        assertThat(request.method(), equalToIgnoringCase("POST"));
        assertThat(request.uri().toURL(), equalTo(new URL("https://example.com/test")));
        assertThat(request.headers().firstValue("Authorization").get(), equalTo("vapid for test"));
        assertThat(request.headers().firstValue("Content-Type").get(),
            equalToIgnoringCase("Application/octet-stream"));
        assertThat(request.headers().firstValue("Content-Encoding").get(), equalTo("aes128gcm"));
        assertThat(request.headers().firstValue("TTL").get(), equalTo("3600"));
        assertThat(request.headers().firstValue("Urgency").get(), equalTo("high"));
        assertThat(request.headers().firstValue("Topic").get(), equalTo("MyTopic"));
        assertThat(request.bodyPublisher().get().contentLength(), greaterThan(0L));
    }


    @Test
    public void buildWithDefaultValues()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException {

        String endpoint = "https://example.com/test";

        PushSubscription pushSubscription = createPushSubscription(endpoint);

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(null);

        HttpRequest request = StandardHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .build(vapidKeyPair)
            .toRequest();

        assertThat(request.method(), equalToIgnoringCase("POST"));
        assertThat(request.uri().toURL(), equalTo(new URL("https://example.com/test")));
        assertThat(request.headers().firstValue("Authorization").get(), equalTo("vapid for test"));
        assertThat(request.headers().firstValue("Content-Type").isPresent(), is(false));
        assertThat(request.headers().firstValue("Content-Encoding").isPresent(), is(false));
        assertThat(request.headers().firstValue("TTL").get(),
            equalTo(String.valueOf(24 * 60 * 60)));
        assertThat(request.headers().firstValue("Urgency").get(), equalTo("normal"));
        assertThat(request.headers().firstValue("Topic").isPresent(), is(false));
        assertThat(request.bodyPublisher().get().contentLength(), equalTo(0L));
    }

}
