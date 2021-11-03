package com.zerodeplibs.webpush.httpclient;

import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;
import static com.zerodeplibs.webpush.TestAssertionUtil.assertStateCheck;
import static com.zerodeplibs.webpush.httpclient.PreparerTestUtil.createPushSubscription;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

/**
 * This test class includes the tests for {@link PreparerBuilder} itself.
 */
public class OkHttpClientRequestPreparerTests {

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

        Request request = OkHttpClientRequestPreparer.getBuilder()
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


        assertThat(request.url().url(), equalTo(new URL("https://example.com/test")));
        assertThat(request.header("Authorization"), equalTo("vapid for test"));
        assertThat(request.header("Content-Type"), equalToIgnoringCase("Application/octet-stream"));
        assertThat(request.header("Content-Encoding"), equalTo("aes128gcm"));
        assertThat(request.header("TTL"), equalTo("3600"));
        assertThat(request.header("Urgency"), equalTo("high"));
        assertThat(request.header("Topic"), equalTo("MyTopic"));
        assertThat(request.body().contentLength(), greaterThan(0L));
    }


    @Test
    public void buildWithDefaultValues()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, MalformedURLException {

        String endpoint = "https://example.com/test";

        PushSubscription pushSubscription = createPushSubscription(endpoint);

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(null);

        Request request = OkHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .build(vapidKeyPair)
            .toRequest();

        assertThat(request.url().url(), equalTo(new URL("https://example.com/test")));
        assertThat(request.header("Authorization"), equalTo("vapid for test"));
        assertThat(request.header("Content-Type"), is(nullValue()));
        assertThat(request.header("Content-Encoding"), is(nullValue()));
        assertThat(request.header("TTL"), equalTo(String.valueOf(24 * 60 * 60)));
        assertThat(request.header("Urgency"), equalTo("normal"));
        assertThat(request.header("Topic"), is(nullValue()));
        assertThat(request.body(), is(nullValue()));
    }

    @Test
    public void shouldThrowExceptionWhenIllegalPushSubscriptionsArePassed() {

        PreparerBuilder<OkHttpClientRequestPreparer> builder = OkHttpClientRequestPreparer.getBuilder();
        PushSubscription pushSubscription = new PushSubscription();
        assertNullCheck(() -> builder.pushSubscription(null), "pushSubscription");
        assertNullCheck(() -> builder.pushSubscription(pushSubscription),
            "pushSubscription.endpoint");

        pushSubscription.setEndpoint("https://example.com");
        assertNullCheck(() -> builder.pushSubscription(pushSubscription), "pushSubscription.keys");

        PushSubscription.Keys keys = new PushSubscription.Keys();
        pushSubscription.setKeys(keys);

        assertNullCheck(() -> builder.pushSubscription(pushSubscription),
            "pushSubscription.keys.p256dh");

        keys.setP256dh("p256dh");
        assertNullCheck(() -> builder.pushSubscription(pushSubscription),
            "pushSubscription.keys.auth");

    }

    @Test
    public void shouldThrowExceptionWhenIllegalTTLsArePassed() {

        PreparerBuilder<OkHttpClientRequestPreparer> builder = OkHttpClientRequestPreparer.getBuilder();
        assertNullCheck(() -> builder.ttl(1, null), "timeUnit");
    }

    @Test
    public void shouldThrowExceptionWhenIllegalUrgenciesArePassed() {
        PreparerBuilder<OkHttpClientRequestPreparer> builder = OkHttpClientRequestPreparer.getBuilder();
        assertNullCheck(() -> builder.urgency(null), "urgencyOption");
    }

    @Test
    public void shouldThrowExceptionWhenBuildingWithoutSufficientParameters() {
        String endpoint = "https://example.com/test";
        Instant expirationTime = Instant.now();

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(VAPIDJWTParam.getBuilder()
                .resourceURLString(endpoint)
                .expirationTime(expirationTime)
                .build()
            );

        PreparerBuilder<OkHttpClientRequestPreparer> builder =
            OkHttpClientRequestPreparer.getBuilder()
                .vapidJWTExpirationTime(expirationTime);


        assertStateCheck(() -> builder.build(vapidKeyPair),
            "The push subscription isn't specified.");
    }

    @Test
    public void shouldThrowExceptionWhenBuildingWithIllegalVAPIDKeyPairs() {
        PreparerBuilder<OkHttpClientRequestPreparer> builder = OkHttpClientRequestPreparer.getBuilder();
        assertNullCheck(() -> builder.build(null), "vapidKeyPair");
    }
}
