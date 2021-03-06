package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.key.PublicKeySources;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public class MessageEncryptionTestUtil {

    public static KeyPair generateKeyPair()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));

        return keyPairGenerator.genKeyPair();
    }

    public static String generateP256dhString(ECPublicKey uaPublic) {
        byte[] uncompressedBytes =
            PublicKeySources.ofECPublicKey(uaPublic).extractBytesInUncompressedForm();
        return toBase64Url(uncompressedBytes);
    }

    public static String generateAuthSecretString() {
        byte[] authSecret = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(authSecret);
        return toBase64Url(authSecret);
    }

    private static String toBase64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
