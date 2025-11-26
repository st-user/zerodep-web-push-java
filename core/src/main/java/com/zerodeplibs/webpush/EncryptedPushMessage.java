package com.zerodeplibs.webpush;

/**
 * Implementations of this interface represent an encrypted push message.
 *
 * <div><b>Thread Safety:</b></div>
 *
 * <p>
 * Depends on implementations.
 * Typically, an implementation of this interface is thread-safe.
 * See {@link MessageEncryptions}.
 * </p>
 *
 * @author Tomoki Sato
 */
public interface EncryptedPushMessage {

    /**
     * Converts this encrypted push message to the octet sequence.
     * This octet sequence is typically put into the body of an HTTP request to a push service.
     *
     * @return the octet sequence that represents this encrypted push message.
     */
    byte[] toBytes();

    /**
     * Returns the length of this encrypted push message.
     *
     * <p>
     * Typically, the returned value is used to set the "Content-Length" HTTP header field.
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
     * Typically, the returned value is used to set the "Content-Encoding" HTTP header field.
     * </p>
     *
     * @return the content encoding of this encrypted push message.
     */
    String contentEncoding();

    /**
     * Returns the media type of this encrypted push message
     * (e.g. "application/octet-stream").
     *
     * <p>
     * Typically, the returned value is used to set the "Content-Type" HTTP header field.
     * </p>
     *
     * @return the media type of this encrypted push message.
     */
    default String mediaType() {
        return "application/octet-stream";
    }
}
