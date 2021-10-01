package com.zerodeplibs.webpush;


import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;

import org.junit.jupiter.api.Test;


public class UserAgentMessageEncryptionKeyInfoTests {

    @Test
    public void shouldThrowExceptionWhenNullReferencesArePassed() {

        assertNullCheck(() -> UserAgentMessageEncryptionKeyInfo.from(null), "subscriptionKeys");

        assertNullCheck(() -> UserAgentMessageEncryptionKeyInfo.of(null, ""), "p256dh");
        assertNullCheck(() -> UserAgentMessageEncryptionKeyInfo.of("", null), "auth");

        assertNullCheck(() -> UserAgentMessageEncryptionKeyInfo.of(null, new byte[] {0}), "p256dh");
        assertNullCheck(() -> UserAgentMessageEncryptionKeyInfo.of(new byte[] {0}, null), "auth");
    }


}
