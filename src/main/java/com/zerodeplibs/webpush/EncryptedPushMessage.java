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

    /**
     * Returns the length of this encrypted push message.
     *
     * <p>
     * Typically, the returned value is set to the "Content-Length" HTTP header field.
     * </p>
     *
     * @return the length of this encrypted push message.
     */
    int length();

    /**
     * Returns the content encoding of this encrypted push message
     * (e.g. "aes128gcm").
     *
     * <p>
     * Typically, the returned value is set to the "Content-Encoding" HTTP header field.
     * </p>
     *
     * @return the content encoding of this encrypted push message.
     */
    String contentEncoding();
}
