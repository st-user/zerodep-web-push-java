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
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Test;

public class JettyHttpClientRequestPreparerTests {


    @Test
    public void buildWithSpecifiedProperties()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
        MalformedURLException {

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

        Request request = JettyHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .vapidJWTExpirationTime(expirationTime)
            .vapidJWTSubject(subject)
            .vapidJWTAdditionalClaim("claimName", "claimValue")
            .pushMessage("Hello World")
            .ttl(1, TimeUnit.HOURS)
            .urgencyHigh()
            .topic("MyTopic")
            .build(vapidKeyPair)
            .toRequest(new HttpClient());


        assertThat(request.getURI().toURL(), equalTo(new URL("https://example.com/test")));
        assertThat(request.getHeaders().get("Authorization"), equalTo("vapid for test"));
        assertThat(request.getHeaders().get("Content-Encoding"), equalTo("aes128gcm"));
        assertThat(request.getHeaders().get("TTL"), equalTo("3600"));
        assertThat(request.getHeaders().get("Urgency"), equalTo("high"));
        assertThat(request.getHeaders().get("Topic"), equalTo("MyTopic"));
        assertThat(request.getBody().getContentType(),
            equalToIgnoringCase("application/octet-stream"));
        assertThat(request.getBody().getLength(), greaterThan(0L));
    }

    @Test
    public void buildWithDefaultValue()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
        MalformedURLException {


        String endpoint = "https://example.com/test";

        PushSubscription pushSubscription = createPushSubscription(endpoint);

        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(null);

        Request request = JettyHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .build(vapidKeyPair)
            .toRequest(new HttpClient());

        assertThat(request.getURI().toURL(), equalTo(new URL("https://example.com/test")));
        assertThat(request.getHeaders().get("Authorization"), equalTo("vapid for test"));
        assertThat(request.getHeaders().get("Content-Type"), is(nullValue()));
        assertThat(request.getHeaders().get("Content-Encoding"), is(nullValue()));
        assertThat(request.getHeaders().get("TTL"), equalTo(String.valueOf(24 * 60 * 60)));
        assertThat(request.getHeaders().get("Urgency"), equalTo("normal"));
        assertThat(request.getHeaders().get("Topic"), is(nullValue()));
        assertThat(request.getBody(), is(nullValue()));
    }
}
