package com.zerodeplibs.webpush;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.Test;

public class PushSubscriptionTests {

    @Test
    public void twoObjectsShouldBeComparedWithEachOtherBasedOnTheirProperties() {

        PushSubscription a = createInstance(
            "1",
            100L,
            "a",
            "b"
        );

        PushSubscription b = createInstance(
            "1",
            100L,
            "a",
            "b"
        );

        assertThat(a.equals(null), equalTo(false));
        assertThat(a.equals(new Object()), equalTo(false));
        assertThat(a.getKeys().equals(null), equalTo(false));
        assertThat(a.getKeys().equals(new Object()), equalTo(false));

        assertThat(a.equals(b), equalTo(true));
        assertThat(a.hashCode(), equalTo(b.hashCode()));

        // Each property
        //// endpoint
        assertThat(
            createInstance(
                "1", null, null, null
            ).equals(
                createInstance(
                    "1", null, null, null
                )), equalTo(true));

        assertThat(
            createInstance(
                "1", null, null, null
            ).equals(
                createInstance(
                    "2", null, null, null
                )), equalTo(false));

        //// expirationTime
        assertThat(
            createInstance(
                null, 100L, null, null
            ).equals(
                createInstance(
                    null, 100L, null, null
                )), equalTo(true));

        assertThat(
            createInstance(
                null, 100L, null, null
            ).equals(
                createInstance(
                    null, 101L, null, null
                )), equalTo(false));

        //// keys.p256dh
        assertThat(
            createInstance(
                null, null, "a", null
            ).equals(
                createInstance(
                    null, null, "a", null
                )), equalTo(true));

        assertThat(
            createInstance(
                null, null, "a", null
            ).equals(
                createInstance(
                    null, null, "b", null
                )), equalTo(false));

        //// keys.auth
        assertThat(
            createInstance(
                null, null, null, "x"
            ).equals(
                createInstance(
                    null, null, null, "x"
                )), equalTo(true));

        //// keys.auth
        assertThat(
            createInstance(
                null, null, null, "x"
            ).equals(
                createInstance(
                    null, null, null, "y"
                )), equalTo(false));
    }


    @Test
    public void toStringShouldReturnDescriptionBasedOnProperties() {

        PushSubscription subscription = createInstance(
            "1",
            100L,
            "a",
            "b"
        );

        assertThat(subscription.toString(), equalTo(
            "PushSubscription{"
            + "endpoint='1'"
            + ", expirationTime='100'"
            + ", keys=Keys{"
            + "p256dh='a'"
            + ", auth='b'}"
            + "}"
        ));
    }

    private PushSubscription createInstance(
        String endpoint,
        Long expirationTime,
        String p256dh,
        String auth
    ) {
        PushSubscription pushSubscription = new PushSubscription();
        pushSubscription.setEndpoint(endpoint);
        pushSubscription.setExpirationTime(expirationTime);

        if (p256dh != null || auth != null) {
            PushSubscription.Keys keys = new PushSubscription.Keys();
            keys.setP256dh(p256dh);
            keys.setAuth(auth);
            pushSubscription.setKeys(keys);
        }
        return pushSubscription;
    }
}
