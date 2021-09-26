package com.zerodeplibs.webpush;

/**
 * Implementations of this interface represent an encrypted push message.
 *
 * @author Tomoki Sato
 */
public interface EncryptedPushMessage {

    /**
     * Converts this push message to the byte array.
     *
     * @return the byte array.
     */
    byte[] toBytes();
}
