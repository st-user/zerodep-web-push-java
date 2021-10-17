package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

/**
 * An implementation of the PrivateKeySource that wraps a byte array.
 *
 * @author Tomoki Sato
 */
class BytesPrivateKeySource implements PrivateKeySource {

    private final byte[] pkcs8Bytes;

    BytesPrivateKeySource(byte[] pkcs8Bytes) {
        WebPushPreConditions.checkNotNull(pkcs8Bytes, "pkcs8Bytes");
        this.pkcs8Bytes = Arrays.copyOf(pkcs8Bytes, pkcs8Bytes.length);
    }

    @Override
    public ECPrivateKey extract() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return (ECPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Bytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw KeyExtractionException.withDefaultMessage(e);
        }
    }
}
