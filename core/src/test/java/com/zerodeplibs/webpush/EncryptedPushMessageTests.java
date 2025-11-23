package com.zerodeplibs.webpush;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.Test;

public class EncryptedPushMessageTests {

    @Test
    public void twoObjectsShouldBeComparedWithEachOtherBasedOnTheirProperties() {

        Aes128GcmEncryptedMessage a =
            new Aes128GcmEncryptedMessage(new byte[] {1, 2});

        Aes128GcmEncryptedMessage b =
            new Aes128GcmEncryptedMessage(new byte[] {1, 2});

        assertThat(a.equals(null), equalTo(false));
        assertThat(a.equals(new Object()), equalTo(false));

        assertThat(a.equals(b), equalTo(true));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
    }

    @Test
    public void toStringShouldReturnDescriptionBasedOnProperties() {

        Aes128GcmEncryptedMessage message =
            new Aes128GcmEncryptedMessage(new byte[] {1, 2});

        assertThat(message.toString(), equalTo(
            "Aes128GcmEncryptedMessage{"
                + "contentEncoding='aes128gcm'"
                + ", length='2'"
                + ", mediaType='application/octet-stream'"
                + "}"
        ));
    }
}
