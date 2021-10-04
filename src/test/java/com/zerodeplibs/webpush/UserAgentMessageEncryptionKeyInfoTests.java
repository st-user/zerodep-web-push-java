package com.zerodeplibs.webpush;


import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateAuthSecretString;
import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateKeyPair;
import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateP256dhString;
import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
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

    @Test
    public void twoObjectsShouldBeComparedWithEachOtherBasedOnTheirProperties()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        KeyPair uaKeyPair = generateKeyPair();
        ECPublicKey uaPublic = (ECPublicKey) uaKeyPair.getPublic();
        String p256dh = generateP256dhString(uaPublic);
        String auth = generateAuthSecretString();

        UserAgentMessageEncryptionKeyInfo a = UserAgentMessageEncryptionKeyInfo.of(
            p256dh, auth
        );
        UserAgentMessageEncryptionKeyInfo b = UserAgentMessageEncryptionKeyInfo.of(
            p256dh, auth
        );

        assertThat(a.equals(null), equalTo(false));
        assertThat(a.equals(new Object()), equalTo(false));

        assertThat(a.equals(b), equalTo(true));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
    }

    @Test
    public void toStringShouldReturnDescriptionBasedOnProperties()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        KeyPair uaKeyPair = generateKeyPair();
        ECPublicKey uaPublic = (ECPublicKey) uaKeyPair.getPublic();
        String p256dh = generateP256dhString(uaPublic);
        String auth = generateAuthSecretString();

        UserAgentMessageEncryptionKeyInfo uaKeys = UserAgentMessageEncryptionKeyInfo.of(
            p256dh, auth
        );

        assertThat(uaKeys.toString(), equalTo(
            "UserAgentMessageEncryptionKeyInfo{"
                + "p256dh='" + p256dh + "'"
                + ", auth='" + auth + "'}"
        ));
    }

    private String toBase64Url(String text) {
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}
