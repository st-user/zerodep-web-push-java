package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.security.interfaces.ECPublicKey;
import java.util.function.Consumer;

/**
 * An implementation of the PublicKeySource that wraps an ECPublicKey.
 *
 * @author Tomoki Sato
 */
class KeyObjectPublicKeySource implements PublicKeySource {

    private final ECPublicKey publicKey;
    private final Consumer<ECPublicKey> publicKeyPostProcessor;
    private boolean processed = false;

    KeyObjectPublicKeySource(ECPublicKey publicKey, Consumer<ECPublicKey> publicKeyPostProcessor) {
        WebPushPreConditions.checkNotNull(publicKey, "publicKey");
        WebPushPreConditions.checkNotNull(publicKeyPostProcessor, "publicKeyPostProcessor");

        this.publicKey = publicKey;
        this.publicKeyPostProcessor = publicKeyPostProcessor;
    }

    @Override
    public ECPublicKey extract() {
        this.processed();
        return this.publicKey;
    }

    @Override
    public byte[] extractUncompressedBytes() {
        this.processed();
        return ECPublicKeyUtil.encodedBytesToUncompressedBytes(this.publicKey.getEncoded());
    }

    private void processed() {
        if (!this.processed) {
            publicKeyPostProcessor.accept(this.publicKey);
            this.processed = true;
        }
    }
}
