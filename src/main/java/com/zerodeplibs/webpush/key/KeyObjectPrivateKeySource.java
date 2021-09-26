package com.zerodeplibs.webpush.key;

import java.security.interfaces.ECPrivateKey;

/**
 * An implementation of the PrivateKeySource that wraps an ECPrivateKey.
 *
 * @author Tomoki Sato
 */
class KeyObjectPrivateKeySource implements PrivateKeySource {

    private final ECPrivateKey privateKey;

    KeyObjectPrivateKeySource(ECPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public ECPrivateKey extract() {
        return this.privateKey;
    }
}
