package com.zerodeplibs.webpush;

import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateAuthSecretString;
import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateKeyPair;
import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateP256dhString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import org.junit.jupiter.api.Test;

public class MessageEncryptionTests {


    @Test
    public void encryptShouldEncryptPayloadUsingSuppliedSubscriptionKeys() throws Exception {


        KeyPair uaKeyPair = generateKeyPair();
        ECPublicKey uaPublic = (ECPublicKey) uaKeyPair.getPublic();
        ECPrivateKey uaPrivate = (ECPrivateKey) uaKeyPair.getPrivate();
        String p256dh = generateP256dhString(uaPublic);
        String auth = generateAuthSecretString();
        String payload = "Hello World. This is a payload for testing.";

        UserAgentMessageEncryptionKeys userAgentMessageEncryptionKeys =
            UserAgentMessageEncryptionKeys.of(p256dh, auth);
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
}
