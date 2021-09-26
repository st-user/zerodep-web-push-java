package com.zerodeplibs.webpush;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.Test;

public class PushSubscriptionTests {

    @Test
    public void equalsShouldCompareTheTwoObjectsBasedOnTheirProperties() {

        PushSubscription a = new PushSubscription();
        PushSubscription.Keys keysForA = new PushSubscription.Keys();
        a.setEndpoint("1");
        keysForA.setP256dh("a");
        a.setKeys(keysForA);

        PushSubscription b = new PushSubscription();
        PushSubscription.Keys keysForB = new PushSubscription.Keys();
        b.setEndpoint("1");
        keysForB.setP256dh("a");
        b.setKeys(keysForB);

        assertThat(a, equalTo(b));
    }
}
