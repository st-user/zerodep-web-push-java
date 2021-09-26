package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.WebPushRuntimeWrapperException;
import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * An implementation of the PublicKeySource that wraps a byte array.
 *
 * @author Tomoki Sato
 */
class BytesPublicKeySource implements PublicKeySource {

    private ECPublicKey publicKey;
    private final byte[] x509Bytes;
    private final Consumer<ECPublicKey> publicKeyPostProcessor;

    static BytesPublicKeySource ofUncompressed(byte[] uncompressedBytes,
                                               Consumer<ECPublicKey> publicKeyPostProcessor) {
        WebPushPreConditions.checkNotNull(uncompressedBytes, "uncompressed bytes for public key");

        if (uncompressedBytes[0] != 0x04 || uncompressedBytes.length != 65) {
            throw new IllegalArgumentException(
                "uncompressed bytes for public key must start with 0x04 and its length must be 65 bytes.");
        }
        return new BytesPublicKeySource(
            ECPublicKeyUtil.uncompressedBytesToX509Bytes(uncompressedBytes),
            publicKeyPostProcessor);
    }

    static BytesPublicKeySource ofX509(byte[] x509Bytes,
                                       Consumer<ECPublicKey> publicKeyPostProcessor) {
        return new BytesPublicKeySource(x509Bytes, publicKeyPostProcessor);
    }

    private BytesPublicKeySource(byte[] x509Bytes, Consumer<ECPublicKey> publicKeyPostProcessor) {
        this.x509Bytes = Arrays.copyOf(x509Bytes, x509Bytes.length);
        this.publicKeyPostProcessor = publicKeyPostProcessor;
    }

    private void extractECPublicKey() {
        if (this.publicKey != null) {
            return;
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPublicKey ecPublicKey =
                (ECPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(this.x509Bytes));
            this.publicKeyPostProcessor.accept(ecPublicKey);
            this.publicKey = ecPublicKey;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new WebPushRuntimeWrapperException(e);
        }
    }

    @Override
    public ECPublicKey extract() {
        this.extractECPublicKey();
        return this.publicKey;
    }

    @Override
    public byte[] extractUncompressedBytes() {
        this.extractECPublicKey();
        return ECPublicKeyUtil.encodedBytesToUncompressedBytes(this.x509Bytes);
    }
}
