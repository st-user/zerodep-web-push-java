package com.zerodeplibs.webpush;

/**
 * Implementations of this interface represent an encrypted push message.
 *
 * @author Tomoki Sato
 */
public interface EncryptedPushMessage {

    /**
     * Converts this encrypted push message to an octet sequence.
     * This octet sequence is typically set to the body of the HTTP request to the push service.
     *
     * @return an octet sequence that represents this encrypted push message.
     */
    byte[] toBytes();
}
