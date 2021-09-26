package com.zerodeplibs.webpush;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.zerodeplibs.webpush.key.PublicKeySources;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class PayloadEncryptionTests {


    @Test
    public void encryptShouldEncryptPayloadUsingSuppliedSubscriptionKeys() throws Exception {


        KeyPair uaKeyPair = generateKeyPair();
        ECPublicKey uaPublic = (ECPublicKey) uaKeyPair.getPublic();
        ECPrivateKey uaPrivate = (ECPrivateKey) uaKeyPair.getPrivate();
        String p256dh = generateP256dhString(uaPublic);
        String authSecret = generateAuthSecretString();
        String payload = "Hello World. This is a payload for testing.";

        UserAgentMessageEncryptionKeys userAgentMessageEncryptionKeys =
            UserAgentMessageEncryptionKeys.of(p256dh, authSecret);
        MessageEncryption payloadEncryption = MessageEncryptions.of();

        EncryptedPushMessage encrypted = payloadEncryption.encrypt(
            userAgentMessageEncryptionKeys,
            PushMessage.ofUTF8(payload)
        );

        byte[] decypted = ((Aes128GcmMessageEncryption) payloadEncryption).decrypt(
            userAgentMessageEncryptionKeys,
            (Aes128GcmEncryptedMessage) encrypted,
            uaPrivate
        );

        assertThat(new String(decypted, StandardCharsets.UTF_8), equalTo(payload));
    }

    private KeyPair generateKeyPair()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));

        return keyPairGenerator.genKeyPair();
    }

    private String generateP256dhString(ECPublicKey uaPublic) {
        byte[] uncompressedBytes =
            PublicKeySources.ofECPublicKey(uaPublic).extractUncompressedBytes();
        return toBase64Url(uncompressedBytes);
    }

    private String generateAuthSecretString() {
        byte[] authSecret = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(authSecret);
        return toBase64Url(authSecret);
    }

    private String toBase64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
