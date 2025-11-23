package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.security.interfaces.ECPrivateKey;

/**
 * An implementation of the PrivateKeySource that wraps an ECPrivateKey.
 *
 * @author Tomoki Sato
 */
class KeyObjectPrivateKeySource implements PrivateKeySource {

    private final ECPrivateKey privateKey;

    KeyObjectPrivateKeySource(ECPrivateKey privateKey) {
        WebPushPreConditions.checkNotNull(privateKey, "privateKey");

        this.privateKey = privateKey;
    }

    @Override
    public ECPrivateKey extract() {
        return this.privateKey;
    }
}
