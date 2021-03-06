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
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MessageEncryptionTests {

    @BeforeAll
    public static void beforeAll() {
        JCAProviderInitializer.initialize();
    }

    @Test
    public void shouldEncryptMessageUsingTheGivenSubscriptionKeys() throws Exception {


        KeyPair uaKeyPair = generateKeyPair();
        ECPublicKey uaPublic = (ECPublicKey) uaKeyPair.getPublic();
        ECPrivateKey uaPrivate = (ECPrivateKey) uaKeyPair.getPrivate();
        String p256dh = generateP256dhString(uaPublic);
        String auth = generateAuthSecretString();
        String payload = "Hello World. This is a payload for testing.";

        UserAgentMessageEncryptionKeyInfo userAgentMessageEncryptionKeys =
            UserAgentMessageEncryptionKeyInfo.of(p256dh, auth);
        MessageEncryption messageEncryption = MessageEncryptions.of();

        EncryptedPushMessage encrypted = messageEncryption.encrypt(
            userAgentMessageEncryptionKeys,
            PushMessage.ofUTF8(payload)
        );

        byte[] decypted = ((Aes128GcmMessageEncryption) messageEncryption).decrypt(
            userAgentMessageEncryptionKeys,
            (Aes128GcmEncryptedMessage) encrypted,
            uaPrivate
        );

        assertThat(new String(decypted, StandardCharsets.UTF_8), equalTo(payload));
    }

    @Test
    public void shouldThrowExceptionWhenNullReferencesArePassed() {

        MessageEncryption messageEncryption = MessageEncryptions.of();

        assertNullCheck(() -> messageEncryption.encrypt(null, PushMessage.ofUTF8("a")),
            "userAgentMessageEncryptionKeyInfo");

        assertNullCheck(
            () -> messageEncryption.encrypt(createUserAgentMessageEncryptionKeyInfo(), null),
            "pushMessage");
    }

    private UserAgentMessageEncryptionKeyInfo createUserAgentMessageEncryptionKeyInfo()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPair keyPair = generateKeyPair();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        return UserAgentMessageEncryptionKeyInfo.of(
            generateP256dhString(publicKey),
            generateAuthSecretString()
        );
    }
}
