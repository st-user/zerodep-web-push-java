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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        assertThat(getHeader(request, "Authorization"), equalTo("vapid for test"));
        assertThat(getHeader(request, "Content-Type"),
            equalToIgnoringCase("application/octet-stream"));
        assertThat(getHeader(request, "Content-Encoding"), equalTo("aes128gcm"));
        assertThat(getHeader(request, "TTL"), equalTo("3600"));
        assertThat(getHeader(request, "Urgency"), equalTo("high"));
        assertThat(getHeader(request, "Topic"), equalTo("MyTopic"));
        assertThat(request.getContent().getLength(), greaterThan(0L));
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
        assertThat(getHeader(request, "Authorization"), equalTo("vapid for test"));
        assertThat(getHeader(request, "Content-Type"), is(nullValue()));
        assertThat(getHeader(request, "Content-Encoding"), is(nullValue()));
        assertThat(getHeader(request, "TTL"), equalTo(String.valueOf(24 * 60 * 60)));
        assertThat(getHeader(request, "Urgency"), equalTo("normal"));
        assertThat(getHeader(request, "Topic"), is(nullValue()));
        assertThat(request.getContent(), is(nullValue()));
    }

    private String getHeader(Request request, String name) {
        try {

            Method getHeaders = request.getClass().getMethod("getHeaders");
            Object headers = getHeaders.invoke(request);
            Class<?> headerFieldClass = headers.getClass();
            Method get = headerFieldClass.getMethod("get", String.class);
            return (String) get.invoke(headers, name);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldThrowExceptionWhenIllegalHTTPClientsArePassed()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        PushSubscription pushSubscription = createPushSubscription("https://example.com/test");
        PreparerTestUtil.TestingVAPIDKeyPair vapidKeyPair =
            new PreparerTestUtil.TestingVAPIDKeyPair(null);

        JettyHttpClientRequestPreparer preparer = JettyHttpClientRequestPreparer.getBuilder()
            .pushSubscription(pushSubscription)
            .build(vapidKeyPair);

        assertNullCheck(() -> preparer.toRequest(null), "httpClient");
    }
}
